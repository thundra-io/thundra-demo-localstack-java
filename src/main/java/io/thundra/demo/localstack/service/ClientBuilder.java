package io.thundra.demo.localstack.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;

public class ClientBuilder {
    private ClientBuilder() {
        throw new IllegalStateException("Not create instance from utility class");
    }

    public static final String AWS_REGION = System.getenv("AWS_REGION");
    public static final String LOCALSTACK_HOSTNAME = System.getenv("LOCALSTACK_HOSTNAME");
    public static final String LOCALSTACK_ENDPOINT = "http://" + LOCALSTACK_HOSTNAME + ":4566";

    public static AmazonSQS buildSQS() {
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION))
                .build();
    }

    public static AmazonDynamoDB buildDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION))
                .build();
    }

    public static AmazonS3 buildS3() {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION))
                .build();
    }

    public static AWSStepFunctions buildStepFunctions() {
        return AWSStepFunctionsClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION))
                .build();
    }
}
