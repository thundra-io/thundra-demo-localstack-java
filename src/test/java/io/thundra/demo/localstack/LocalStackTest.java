package io.thundra.demo.localstack;

import com.browserstack.local.Local;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.thundra.agent.core.util.StringUtils;
import io.thundra.agent.lambda.localstack.LambdaServer;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.integration.ClientAndServer;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

/**
 * @author tolga
 */
public abstract class LocalStackTest {

    protected static final int ASSERT_EVENTUALLY_TIMEOUT_SECS = 120;
    protected static final int ASSERT_EVENTUALLY_PERIOD_SECS = 10;
    protected static final boolean BROWSERSTACK_ENABLE =
            Boolean.parseBoolean(System.getenv("BROWSERSTACK_ENABLE"));

    protected static BrowserWebDriverContainer browserWebDriverContainer;
    protected static RemoteWebDriver webDriver;
    protected static Local local;

    protected String lambdaUrl;
    protected ClientAndServer mockServerClient;

    protected static String getHostAddress() {
        String os = StringUtils.toLowerCase(System.getProperty("os.name"));
        if (os.indexOf("nix") >= 0
                || os.indexOf("nux") >= 0
                || os.indexOf("aix") > 0) {
            return "172.17.0.1";
        } else {
            return "host.docker.internal";
        }
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        LambdaServer.start();

        if (BROWSERSTACK_ENABLE) {
            webDriver = createBrowserStackWebDriver();
        } else {
            browserWebDriverContainer =
                    new BrowserWebDriverContainer().
                            withCapabilities(
                                    new ChromeOptions().
                                            setHeadless(true).
                                            addArguments("--disable-dev-shm-usage"));
            browserWebDriverContainer.start();
            webDriver = browserWebDriverContainer.getWebDriver();
        }
    }

    private static RemoteWebDriver createBrowserStackWebDriver() throws Exception {
        Map<String, Object> config = new JSONObject(
                new JSONTokener(
                        ClassLoader.getSystemClassLoader().getResourceAsStream("conf/local.conf.json"))).
                toMap();

        DesiredCapabilities capabilities = new DesiredCapabilities();

        Map<String, String> env = (Map<String, String>) config.get("environment");
        Iterator it = env.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
        }

        Map<String, String> commonCapabilities = (Map<String, String>) config.get("capabilities");
        it = commonCapabilities.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (capabilities.getCapability(pair.getKey().toString()) == null) {
                capabilities.setCapability(pair.getKey().toString(), pair.getValue().toString());
            }
        }

        String username = System.getenv("BROWSERSTACK_USERNAME");
        if (username == null) {
            username = (String) config.get("user");
        }

        String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
        if (accessKey == null) {
            accessKey = (String) config.get("key");
        }

        if (Boolean.parseBoolean(String.valueOf(capabilities.getCapability("browserstack.local")))) {
            local = new Local();
            Map<String, String> options = new HashMap();
            options.put("key", accessKey);
            local.start(options);
        }

        return new RemoteWebDriver(
                new URL(
                        "https://" + username + ":" + accessKey +
                                "@" + config.get("server") + "/wd/hub"),
                capabilities);
    }

    @BeforeEach
    public void setup() throws Exception {
        LambdaServer.reset();

        executeCommand("make start-embedded");
        String result = executeCommand("awslocal apigateway get-rest-apis");
        JSONObject object = new JSONObject(result);
        JSONArray array = object.getJSONArray("items");
        String restApiId = array.getJSONObject(0).getString("id");
        lambdaUrl = "http://localhost:4566/restapis/" + restApiId + "/local/_user_request_/requests";

        mockServerClient = startClientAndServer(0);
    }

    @AfterEach
    public void teardown() throws IOException, InterruptedException {
        executeCommand("docker stop $(docker ps -a -q --filter ancestor=localstack/localstack --format=\"{{.ID}}\")");

        if (mockServerClient != null) {
            mockServerClient.stop();
        }
    }

    @AfterAll
    public static void afterAll() throws Exception {
        LambdaServer.stop();

        if (webDriver != null) {
            webDriver.quit();
            webDriver = null;
        }

        if (local != null) {
            local.stop();
            local = null;
        }

        if (browserWebDriverContainer != null) {
            browserWebDriverContainer.close();
            browserWebDriverContainer = null;
        }
    }

    private String executeCommand(String command) throws IOException, InterruptedException {
        return executeCommand(command, null);
    }

    private String executeCommand(String command, Map<String, String> envVars) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();

        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }

        Map<String, String> processEnvVars = builder.environment();
        if (envVars != null) {
            for (Map.Entry<String, String> e : envVars.entrySet()) {
                processEnvVars.put(e.getKey(), e.getValue());
            }
        }

        Process process = builder.start();

        process.waitFor();

        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    protected void assertEventually(Runnable assertTask) {
        assertEventually(assertTask, ASSERT_EVENTUALLY_PERIOD_SECS, ASSERT_EVENTUALLY_TIMEOUT_SECS);
    }

    protected void assertEventually(Runnable assertTask, long periodSecs, long timeoutSecs) {
        long deadline = System.currentTimeMillis() + (timeoutSecs * 1000);
        AssertionError assertionError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(periodSecs * 1000);
            } catch (InterruptedException e) {
            }
            try {
                assertTask.run();
                assertionError = null;
                break;
            } catch (AssertionError e) {
                assertionError = e;
            }
        }
        if (assertionError != null) {
            throw assertionError;
        }
    }

    protected <R> ResponseEntity<R> get(String path, Class<R> responseType) throws IOException {
        HttpUriRequest request = new HttpGet(path);
        return doRequest(request, responseType);
    }

    protected <R> ResponseEntity<R> get(String path, TypeReference<R> responseType) throws IOException {
        HttpUriRequest request = new HttpGet(path);
        return doRequest(request, responseType);
    }

    protected <R> ResponseEntity<R> post(String path, Class<R> responseType) throws IOException {
        HttpUriRequest request = new HttpPost(path);
        return doRequest(request, responseType);
    }

    protected <R> ResponseEntity<R> post(String path, TypeReference<R> responseType) throws IOException {
        HttpUriRequest request = new HttpPost(path);
        return doRequest(request, responseType);
    }

    private <R> ResponseEntity<R> doRequest(HttpUriRequest request, Class<R> responseType) throws IOException {
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        R response = retrieveResourceFromResponse(httpResponse, responseType);
        return new ResponseEntity<>(httpResponse.getStatusLine().getStatusCode(), response);
    }

    private <R> ResponseEntity<R> doRequest(HttpUriRequest request, TypeReference<R> responseType) throws IOException {
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
        R response = retrieveResourceFromResponse(httpResponse, responseType);
        return new ResponseEntity<>(httpResponse.getStatusLine().getStatusCode(), response);
    }

    public static <T> T retrieveResourceFromResponse(HttpResponse response, TypeReference<T> clazz) throws IOException {
        String jsonFromResponse = EntityUtils.toString(response.getEntity());
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(jsonFromResponse, clazz);
    }

    public static <T> T retrieveResourceFromResponse(HttpResponse response, Class<T> clazz) throws IOException {
        if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_OK && response.getStatusLine().getStatusCode() < HttpStatus.SC_BAD_REQUEST) {
            String jsonFromResponse = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(jsonFromResponse, clazz);
        } else {
            return null;
        }
    }

    public class ResponseEntity<R> {

        private int statusCode;
        private R body;

        public ResponseEntity(int statusCode, R body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public R getBody() {
            return body;
        }

        public void setBody(R body) {
            this.body = body;
        }

    }

}
