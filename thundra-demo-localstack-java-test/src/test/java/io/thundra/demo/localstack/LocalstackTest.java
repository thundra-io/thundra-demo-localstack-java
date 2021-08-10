package io.thundra.demo.localstack;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class LocalstackTest {
    protected static final int ASSERT_EVENTUALLY_TIMEOUT_SECS = 30;
    protected static String lambdaUrl;

    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        //executeCommand("cd .. && make start");
        String result = executeCommand("awslocal apigateway get-rest-apis");
        JSONObject object = new JSONObject(result);
        JSONArray array = object.getJSONArray("items");
        String restApiId = array.getJSONObject(0).getString("id");
        lambdaUrl = "http://localhost:4566/restapis/" + restApiId + "/local/_user_request_/requests";
    }

    @AfterAll
    static void teardown() throws IOException, InterruptedException {
        //executeCommand("docker stop $(docker ps -a -q --filter ancestor=localstack/localstack --format=\"{{.ID}}\")");
    }

    private static String executeCommand(String command) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
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
        long deadline = System.currentTimeMillis() + (ASSERT_EVENTUALLY_TIMEOUT_SECS * 1000);
        AssertionError assertionError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(10000);
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
        String jsonFromResponse = EntityUtils.toString(response.getEntity());
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(jsonFromResponse, clazz);
    }

    public class ResponseEntity<R> {
        private int status;
        private R body;

        public ResponseEntity(int status, R body) {
            this.status = status;
            this.body = body;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public R getBody() {
            return body;
        }

        public void setBody(R body) {
            this.body = body;
        }
    }
}
