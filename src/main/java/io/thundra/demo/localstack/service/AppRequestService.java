package io.thundra.demo.localstack.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
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
import java.util.Map;

import static io.thundra.demo.localstack.service.ClientBuilder.LOCALSTACK_HOSTNAME;
import static io.thundra.demo.localstack.service.ClientBuilder.buildDynamoDB;
import static io.thundra.demo.localstack.service.ClientBuilder.buildS3;
import static io.thundra.demo.localstack.service.ClientBuilder.buildSNS;
import static io.thundra.demo.localstack.service.ClientBuilder.buildSQS;
import static io.thundra.demo.localstack.service.Utils.generateShortUuid;

public class AppRequestService {
    private final ObjectMapper mapper = new ObjectMapper();
    public static final String QUEUE_URL = System.getenv("QUEUE_URL");
    public static final String SNS_ARN = System.getenv("SNS_ARN");
    private static final String S3_BUCKET = System.getenv("S3_BUCKET");
    private static final String TABLE_NAME = System.getenv("TABLE_NAME");

    private final AmazonSQS sqs;
    private final AmazonSNS sns;
    private final AmazonDynamoDB dynamoDB;
    private final AmazonS3 s3;

    public AppRequestService() {
        this.sqs = buildSQS();
        this.sns = buildSNS();
        this.dynamoDB = buildDynamoDB();
        this.s3 = buildS3();
    }

    public String generateRequestId() {
        return generateShortUuid();
    }

    public void sendAppRequestMessage(String requestId) throws JsonProcessingException {
        Message message = new Message(requestId);
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withMessageBody(mapper.writeValueAsString(message))
                .withQueueUrl(QUEUE_URL);
        sqs.sendMessage(sendMessageRequest);
    }

    public void sendAppRequestNotification(String requestId) throws JsonProcessingException {
        Message message = new Message(requestId);
        PublishRequest publishRequest = new PublishRequest()
                .withMessage(mapper.writeValueAsString(message))
                .withTopicArn(SNS_ARN);
        sns.publish(publishRequest);
    }

    public void addAppRequest(String requestId, String status) {
        AppRequests item = new AppRequests(generateShortUuid(), requestId, "" + Calendar.getInstance().getTime(), status);
        DynamoDBMapper dynamoDBMapper = buildDynamoDBMapper();
        dynamoDBMapper.save(item);
    }

    public List<AppRequests> listAppRequests() {
        DynamoDBMapper dynamoDBMapper = buildDynamoDBMapper();
        return dynamoDBMapper.scan(AppRequests.class, new DynamoDBScanExpression());
    }

    public void archiveAppRequest(String requestId) {
        s3.putObject(S3_BUCKET, requestId + "/result.txt", "Archive result for request " + requestId);
    }

    public String getRequestId(String body) throws IOException {
        Message event = mapper.readValue(body, Message.class);
        return event.getRequestId();
    }

    private DynamoDBMapper buildDynamoDBMapper() {
        return new DynamoDBMapper(dynamoDB, DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(TABLE_NAME))
                .build());
    }
}
