package io.thundra.demo.localstack.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.thundra.demo.localstack.model.AppRequests;
import io.thundra.demo.localstack.model.Message;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static io.thundra.demo.localstack.service.ClientBuilder.LOCALSTACK_HOSTNAME;
import static io.thundra.demo.localstack.service.ClientBuilder.buildDynamoDB;
import static io.thundra.demo.localstack.service.ClientBuilder.buildS3;
import static io.thundra.demo.localstack.service.ClientBuilder.buildSNS;
import static io.thundra.demo.localstack.service.ClientBuilder.buildSQS;
import static io.thundra.demo.localstack.service.Utils.generateShortUuid;

public class AppRequestService {
    private final ObjectMapper mapper = new ObjectMapper();
    public static final String QUEUE_URL = "http://" + LOCALSTACK_HOSTNAME + ":4566/000000000000/requestQueue";
    public static final String SNS_ARN = "arn:aws:sns:us-east-1:000000000000:requestTopic";

    private static final String S3_BUCKET = "archivebucket";


    public String generateRequestId() {
        return generateShortUuid();
    }

    public void sendAppRequestMessage(String requestId) throws JsonProcessingException {
        AmazonSQS sqs = buildSQS();
        Message message = new Message(requestId);
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withMessageBody(mapper.writeValueAsString(message))
                .withQueueUrl(QUEUE_URL);
        sqs.sendMessage(sendMessageRequest);
    }

    public void sendAppRequestNotification(String requestId) throws JsonProcessingException {
        AmazonSNS sns = buildSNS();
        Message message = new Message(requestId);
        PublishRequest publishRequest = new PublishRequest()
                .withMessage(mapper.writeValueAsString(message))
                .withTopicArn(SNS_ARN);
        sns.publish(publishRequest);
    }

    public void addAppRequest(String requestId, String status) {
        AmazonDynamoDB dynamoDB = buildDynamoDB();
        AppRequests item = new AppRequests(generateShortUuid(), requestId, "" + Calendar.getInstance().getTime(), status);
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoDB);
        dynamoDBMapper.save(item);
    }

    public List<AppRequests> listAppRequests() {
        AmazonDynamoDB dynamoDB = buildDynamoDB();
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoDB);
        return dynamoDBMapper.scan(AppRequests.class, new DynamoDBScanExpression());
    }

    public void archiveAppRequest(String requestId) {
        AmazonS3 s3 = buildS3();
        s3.putObject(S3_BUCKET, requestId + "/result.txt", "Archive result for request " + requestId);
    }

    public String getRequestId(String body) throws IOException {
        Message event = mapper.readValue(body, Message.class);
        return event.getRequestID();
    }
}