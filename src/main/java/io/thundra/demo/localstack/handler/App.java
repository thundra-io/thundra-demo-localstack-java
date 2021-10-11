package io.thundra.demo.localstack.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.thundra.demo.localstack.model.AppRequest;
import io.thundra.demo.localstack.model.Response;
import io.thundra.demo.localstack.service.AppRequestService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tolga
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LogManager.getLogger(App.class);
    private static final Map<String, String> headers = new HashMap<String, String>() {{
        put("content-type", "application/json");
        put("Access-Control-Allow-Credentials", "true");
    }};

    private final ObjectMapper mapper = new ObjectMapper();
    private AppRequestService appRequestService = new AppRequestService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            logger.info("Request --> " + request);
            if ("/requests".equals(request.getPath()) && "POST".equals(request.getHttpMethod())) {
                return startNewRequest(request);
            } else if ("/requests".equals(request.getPath()) && "GET".equals(request.getHttpMethod())) {
                String requestId = request.getQueryStringParameters().get("id");
                if (requestId != null) {
                    return getRequest(requestId);
                } else {
                    return listRequests();
                }
            } else {
                return new APIGatewayProxyResponseEvent().
                        withStatusCode(404);
            }
        } catch (Exception e) {
            logger.error("Error occurred handling message. Exception is ", e);
            return new APIGatewayProxyResponseEvent().
                    withStatusCode(500).
                    withBody(e.getMessage());
        }
    }

    private String normalizeRequestId(String requestId) {
        // Only allow letters, digits and -
        requestId = requestId.replaceAll("[^a-zA-Z0-9\\-]", "");
        return requestId;
    }

    private APIGatewayProxyResponseEvent startNewRequest(APIGatewayProxyRequestEvent request) throws JsonProcessingException {
        String requestId = request.getQueryStringParameters().get("id");
        if (requestId == null) {
            requestId = appRequestService.generateRequestId();
        } else {
            requestId = normalizeRequestId(requestId);
        }
        // Put message onto SQS queue
        appRequestService.sendAppRequestMessage(requestId);
        // Set status in DynamoDB to "QUEUED"
        String status = "QUEUED";
        appRequestService.createAppRequest(requestId, status);
        return new APIGatewayProxyResponseEvent().
                withStatusCode(200).
                withHeaders(headers).
                withBody(mapper.writeValueAsString(new Response(requestId, status)));
    }

    private APIGatewayProxyResponseEvent getRequest(String requestId) throws JsonProcessingException {
        // TODO There is a bug here.
        // request id must be normalized here while getting as it is already normalized while saving
        //requestId = normalizeRequestId(requestId);
        AppRequest response = appRequestService.getAppRequest(requestId);
        return new APIGatewayProxyResponseEvent().
                withStatusCode(200).
                withHeaders(headers).
                withBody(mapper.writeValueAsString(response));
    }

    private APIGatewayProxyResponseEvent listRequests() throws JsonProcessingException {
        List<AppRequest> response = appRequestService.listAppRequests();
        return new APIGatewayProxyResponseEvent().
                withStatusCode(200).
                withHeaders(headers).
                withBody(mapper.writeValueAsString(response));
    }

}
