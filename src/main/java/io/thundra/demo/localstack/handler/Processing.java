package io.thundra.demo.localstack.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import io.thundra.demo.localstack.model.AppRequest;
import io.thundra.demo.localstack.service.AppRequestService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author tolga
 */
public class Processing implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = LogManager.getLogger(Processing.class);

    private AppRequestService appRequestService = new AppRequestService();

    @Override
    public Void handleRequest(SQSEvent request, Context context) {
        logger.info("Processing request --> " + request);
        List<SQSEvent.SQSMessage> records = request.getRecords();
        records.forEach(sqsMessage -> {
            try {
                String requestId = appRequestService.getRequestId(sqsMessage.getBody());

                // Simulate processing delay
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));

                appRequestService.sendAppRequestNotification(requestId);

                // Simulate some more processing delay
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));

                // Set request status to "PROCESSED"
                appRequestService.updateAppRequest(
                        // Fill only updated attributes
                        new AppRequest().
                                setRequestId(requestId).
                                setProcessedTimestamp(System.currentTimeMillis()).
                                setStatus("PROCESSED"));
            } catch (IOException | InterruptedException e) {
                logger.error("Error occurred handling message. Exception is ", e);
            }
        });
        logger.info("Processed request --> " + request);
        return null;
    }

}
