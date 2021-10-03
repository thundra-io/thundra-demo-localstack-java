package io.thundra.demo.localstack.integration;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import io.thundra.agent.lambda.localstack.LambdaServer;
import io.thundra.demo.localstack.ChaosInjector;
import io.thundra.demo.localstack.LocalStackTest;
import io.thundra.demo.localstack.model.AppRequests;
import io.thundra.demo.localstack.model.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * @author tolga
 */
public class AppRequestLocalStackTest extends LocalStackTest {

    @Test
    public void newRequestShouldBeAbleToCreatedAndProcessed_throughBackendAPI() throws IOException {
        // Inject DynamoDB chaos only for "backend_archiveResult" function
        LambdaServer.registerFunctionEnvironmentInitializer(
                ChaosInjector.createDynamoDBChaosInjector("backend_archiveResult"));

        ResponseEntity<Response> responseEntity = post(lambdaUrl, Response.class);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        Response response = responseEntity.getBody();
        assertThat(response.getRequestId()).isNotBlank();
        assertThat(response.getStatus()).isEqualTo("QUEUED");
        assertEventually(() -> {
            try {
                ResponseEntity<List<AppRequests>> getResponseEntity =
                        get(lambdaUrl, new TypeReference<List<AppRequests>>() {});
                assertThat(getResponseEntity.getStatusCode()).isEqualTo(HttpStatus.SC_OK);

                List<AppRequests> appRequests = getResponseEntity.getBody();
                System.out.println("# of requests: " + appRequests.size());

                assertThat(appRequests.size()).isEqualTo(1);

                AppRequests appRequest = appRequests.get(0);
                System.out.println("Request Id : " + appRequest.getRequestId());
                System.out.println("Status     : " + appRequest.getStatus());

                assertThat(appRequest.getStatus()).isEqualTo("FINISHED");
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void newRequestShouldBeAbleToCreatedAndProcessed_throughFrontendAPI() throws IOException {
        // Inject DynamoDB chaos only for "backend_archiveResult" function
        LambdaServer.registerFunctionEnvironmentInitializer(
                ChaosInjector.createDynamoDBChaosInjector("backend_archiveResult"));

        String hostAddress = getHostAddress();

        String responseContent = IOUtils.toString(new FileInputStream("src/main/static/index.html"));
        responseContent = responseContent.replace("localhost", hostAddress);

        mockServerClient.
                when(request().
                        withMethod("GET").
                        withPath("/index.html")).
                respond(response().
                        withStatusCode(200).
                        withBody(responseContent));

        RemoteWebDriver webDriver = browserWebDriverContainer.getWebDriver();

        String url = "http://" + hostAddress + ":" + mockServerClient.getPort() + "/index.html";
        webDriver.get(url);

        WebElement btnNewReq = webDriver.findElementById("btn-new-req");
        btnNewReq.click();

        assertEventually(() -> {
            try {
                LogEntries logs = webDriver.manage().logs().get(LogType.BROWSER);
                List<LogEntry> logEntries = logs.getAll().
                        stream().
                        filter(e -> e.getLevel().intValue() >= Level.SEVERE.intValue()).
                        collect(Collectors.toList());
                for (LogEntry log : logEntries) {
                    System.out.println(log.getLevel() + " - " + log.getMessage());
                }

                WebElement btnRefresh = webDriver.findElementById("btn-refresh");
                btnRefresh.click();

                List<WebElement> trEvents = webDriver.findElementsByXPath("//tbody[@id='events']/tr");
                System.out.println("# of requests: " + trEvents.size());

                assertThat(trEvents.size()).isEqualTo(1);

                WebElement trEvent = trEvents.get(0);
                List<WebElement> tdEventAttributes = trEvent.findElements(By.tagName("td"));
                WebElement tdRequestId = tdEventAttributes.get(1);
                WebElement tdStatus = tdEventAttributes.get(2);
                System.out.println("Request Id : " + tdRequestId.getText());
                System.out.println("Status     : " + tdStatus.getText());

                assertThat(tdStatus.getText()).isEqualTo("FINISHED");
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
    }

}
