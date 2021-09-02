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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author tolga
 */
public class ClientBuilder {

    private static final Logger logger = LogManager.getLogger(ClientBuilder.class);

    public static final String AWS_REGION = System.getenv("AWS_REGION");
    public static final String LOCALSTACK_HOSTNAME = System.getenv("LOCALSTACK_HOSTNAME");
    public static final String LOCALSTACK_ENDPOINT =
            LOCALSTACK_HOSTNAME != null
                    ? "http://" + LOCALSTACK_HOSTNAME + ":4566"
                    : null;

    private ClientBuilder() {
        throw new UnsupportedOperationException("Not create instance from utility class");
    }

    public static String normalizeUrl(String url) {
        if (LOCALSTACK_ENDPOINT != null) {
            try {
                URL u = new URL(url);
                url = url.replace(u.getHost(), LOCALSTACK_HOSTNAME);
            } catch (MalformedURLException e) {
                logger.error("Invalid URL to normalize: " + url);
            }
        }
        return url;
    }

    private static <T extends AwsClientBuilder<T, ?>> T buildClient(T builder) {
        if (LOCALSTACK_ENDPOINT != null) {
            return builder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(LOCALSTACK_ENDPOINT, AWS_REGION));
        } else {
            return builder;
        }
    }

    public static AmazonSQS buildSQS() {
        return buildClient(AmazonSQSClientBuilder.standard()).build();
    }

    public static AmazonDynamoDB buildDynamoDB() {
        return buildClient(AmazonDynamoDBClientBuilder.standard()).build();
    }

    public static AmazonS3 buildS3() {
        return buildClient(AmazonS3ClientBuilder.standard()).
                withPathStyleAccessEnabled(LOCALSTACK_ENDPOINT != null).
                build();
    }

    public static AmazonSNS buildSNS() {
        return buildClient(AmazonSNSClientBuilder.standard()).build();
    }

}
