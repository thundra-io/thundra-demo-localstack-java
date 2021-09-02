package io.thundra.demo.localstack.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import io.thundra.demo.localstack.LocalStackTest;
import io.thundra.demo.localstack.model.AppRequests;
import io.thundra.demo.localstack.model.Response;
import org.apache.http.HttpStatus;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tolga
 */
public class AppRequestLocalStackTest extends LocalStackTest {

    @Test
    public void testCreateNewRequest() throws IOException {
        ResponseEntity<Response> responseEntity = post(lambdaUrl, Response.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        Response response = responseEntity.getBody();
        assertThat(response.getRequestId()).isNotBlank();
        assertThat(response.getStatus()).isEqualTo("QUEUED");
        assertEventually(() -> {
            try {
                ResponseEntity<List<AppRequests>> getResponseEntity = get(lambdaUrl, new TypeReference<List<AppRequests>>() {
                });
                assertThat(getResponseEntity.getStatusCode()).isEqualTo(HttpStatus.SC_OK);
                List<AppRequests> getResponse = getResponseEntity.getBody();
                assertThat(getResponse)
                        .extracting("requestId", "status")
                        .contains(
                                Tuple.tuple(response.getRequestId(), "QUEUED"),
                                Tuple.tuple(response.getRequestId(), "PROCESSING"),
                                Tuple.tuple(response.getRequestId(), "FINISHED")
                        );
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        });
    }

}
