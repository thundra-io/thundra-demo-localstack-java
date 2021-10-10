package io.thundra.demo.localstack.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
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
public class Archive implements RequestHandler<SNSEvent, Void> {

    private static final Logger logger = LogManager.getLogger(Archive.class);

    private AppRequestService appRequestService = new AppRequestService();

    @Override
    public Void handleRequest(SNSEvent request, Context context) {
        logger.info("Archiving request --> " + request);
        List<SNSEvent.SNSRecord> records = request.getRecords();
        records.forEach(snsRecord -> {
            try {
                String requestId = appRequestService.getRequestId(snsRecord.getSNS().getMessage());

                // Put result onto S3
                appRequestService.archiveAppRequest(requestId);

                // Simulate processing delay
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));

                // Set request status to "FINISHED"
                appRequestService.updateAppRequest(
                        // Fill only updated attributes
                        new AppRequest().
                                setRequestId(requestId).
                                setArchivedTimestamp(System.currentTimeMillis()).
                                setStatus("FINISHED"));
            } catch (IOException | InterruptedException e) {
                logger.error("Error occurred handling message. Exception is ", e);
            }
        });
        logger.info("Archived request --> " + request);
        return null;
    }

}
