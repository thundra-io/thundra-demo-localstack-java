package io.thundra.demo.localstack.service;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class ClientBuilder {

    private ClientBuilder() {
        throw new IllegalStateException("Not create instance from ClientBuilder class");
    }

    public static final String AWS_REGION = System.getenv("AWS_REGION");
    public static final String LOCALSTACK_HOSTNAME = System.getenv("LOCALSTACK_HOSTNAME");
    public static final String LOCALSTACK_ENDPOINT = "http://" + LOCALSTACK_HOSTNAME + ":4566";
    public static final boolean isLocalstackEnv = isLocalstackEnv();

    public static AmazonSQS buildSQS() {
        if (isLocalstackEnv) {
            return AmazonSQSClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION))
                    .build();
        } else {
            return AmazonSQSClientBuilder.standard()
                    .build();
        }
    }

    public static AmazonDynamoDB buildDynamoDB() {
        if (isLocalstackEnv) {
            return AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION))
                    .build();
        } else {
            return AmazonDynamoDBClientBuilder.standard()
                    .build();
        }

    }

    public static AmazonS3 buildS3() {
        if (isLocalstackEnv) {
            return AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION))
                    .build();
        } else {
            return AmazonS3ClientBuilder.standard()
                    .build();
        }
    }

    public static AmazonSNS buildSNS() {
        if (isLocalstackEnv) {
            return AmazonSNSClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION))
                    .build();
        } else {
            return AmazonSNSClientBuilder.standard()
                    .build();
        }

    }

    private static boolean isLocalstackEnv() {
        return LOCALSTACK_HOSTNAME != null && !LOCALSTACK_HOSTNAME.isEmpty();
    }
}
