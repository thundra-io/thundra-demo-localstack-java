package io.thundra.demo.localstack.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * @author tolga
 */
@DynamoDBTable(tableName = "app-requests")// table name must be overridden on each call
public class AppRequests {

    private String requestId;
    private long timestamp;
    private String status;

    public AppRequests() {
    }

    public AppRequests(String requestId, long timestamp, String status) {
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.status = status;
    }

    @DynamoDBHashKey(attributeName = "requestId")
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @DynamoDBAttribute(attributeName = "timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AppRequests{" +
                "requestId='" + requestId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}
