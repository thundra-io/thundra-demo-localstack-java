package io.thundra.demo.localstack.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import io.thundra.demo.localstack.model.AppRequests;
import io.thundra.demo.localstack.LocalstackTest;
import io.thundra.demo.localstack.model.Response;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class AppRequestLocalstackTest extends LocalstackTest {

    @Test
    public void testCreateNewRequest() throws IOException {
        ResponseEntity<Response> responseEntity = post(lambdaUrl, Response.class);
        assertThat(responseEntity.getStatus()).isEqualTo(HttpStatus.SC_OK);
        Response response = responseEntity.getBody();
        Assertions.assertThat(response.getRequestID()).isNotBlank();
        Assertions.assertThat(response.getStatus()).isEqualTo("QUEUED");
        assertEventually(() -> {
            try {
                ResponseEntity<List<AppRequests>> getResponseEntity = get(lambdaUrl, new TypeReference<List<AppRequests>>() {
                });
                assertThat(getResponseEntity.getStatus()).isEqualTo(HttpStatus.SC_OK);
                List<AppRequests> getResponse = getResponseEntity.getBody();
                Assertions.assertThat(getResponse)
                        .extracting("requestId", "status")
                        .contains(
                                Tuple.tuple(response.getRequestID(), "QUEUED"),
                                Tuple.tuple(response.getRequestID(), "PROCESSING"),
                                Tuple.tuple(response.getRequestID(), "FINISHED")
                        );
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        });
    }
}
