package io.thundra.demo.localstack.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.thundra.demo.localstack.service.AppRequestService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Archive implements RequestHandler<Map<String, String>, Map<String, String>> {
    private static final Logger logger = LogManager.getLogger(Archive.class);
    private AppRequestService appRequestService = new AppRequestService();

    @Override
    public Map<String,String> handleRequest(Map<String,String> request, Context context) {
        try {
            logger.info("Archive Handle Request -->" + request);
            String requestId = appRequestService.getRequestId(request);
            //put result onto S3
            appRequestService.archiveAppRequest(requestId);
            //simulate processing delay
            Thread.sleep(TimeUnit.SECONDS.toMillis(3));
            //set request status to FINISHED
            appRequestService.addAppRequest(requestId, "FINISHED");
            return request;
        } catch (IOException | InterruptedException e) {
            logger.error("Error occurred handling message. Exception is ", e);
            throw new RuntimeException(e);
        }
    }
}
