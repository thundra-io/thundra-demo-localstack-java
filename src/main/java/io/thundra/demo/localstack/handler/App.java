package io.thundra.demo.localstack.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.thundra.demo.localstack.model.AppRequests;
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
    }};

    private final ObjectMapper mapper = new ObjectMapper();
    private AppRequestService appRequestService = new AppRequestService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            logger.info("Request --> " + request);
            if ("/requests".equals(request.getPath()) && "POST".equals(request.getHttpMethod())) {
                return startNewRequest();
            } else if ("/requests".equals(request.getPath()) && "GET".equals(request.getHttpMethod())) {
                return listRequests();
            } else {
                return new APIGatewayProxyResponseEvent().
                        withStatusCode(404);
            }
        } catch (JsonProcessingException e) {
            logger.error("Error occurred handling message. Exception is ", e);
            return new APIGatewayProxyResponseEvent().
                    withStatusCode(500).
                    withBody(e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent startNewRequest() throws JsonProcessingException {
        // put message onto SQS queue
        String requestId = appRequestService.generateRequestId();
        appRequestService.sendAppRequestMessage(requestId);
        // set status in DynamoDB to QUEUED
        String status = "QUEUED";
        appRequestService.addAppRequest(requestId, status);
        return new APIGatewayProxyResponseEvent().
                withStatusCode(200).
                withHeaders(headers).
                withBody(mapper.writeValueAsString(new Response(requestId, status)));
    }

    private APIGatewayProxyResponseEvent listRequests() throws JsonProcessingException {
        List<AppRequests> response = appRequestService.listAppRequests();
        return new APIGatewayProxyResponseEvent().
                withStatusCode(200).
                withHeaders(headers).
                withBody(mapper.writeValueAsString(response));
    }

}
