package io.thundra.demo.localstack.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.thundra.demo.localstack.service.AppRequestService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Processing implements RequestHandler<Map<String, String>, Map<String, String>> {
    private static final Logger logger = LogManager.getLogger(Processing.class);
    private AppRequestService appRequestService = new AppRequestService();

    @Override
    public Map<String, String> handleRequest(Map<String, String> request, Context context) {
        try {
            logger.info("Processing Handle Request -->" + request);
            String requestId = appRequestService.getRequestId(request);
            //simulate queueing delay
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            //set request status to PROCESSING
            appRequestService.addAppRequest(requestId, "PROCESSING");
            //simulate processing delay
            Thread.sleep(TimeUnit.SECONDS.toMillis(4));
            return request;
        } catch (IOException | InterruptedException e) {
            logger.error("Error occurred handling message. Exception is ", e);
            throw new RuntimeException(e);
        }
    }


}
