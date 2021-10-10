package io.thundra.demo.localstack.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * @author tolga
 */
@DynamoDBTable(tableName = "app-requests") // table name must be overridden on each call
public class AppRequest {

    private String requestId;
    private Long timestamp;
    private Long processedTimestamp;
    private Long archivedTimestamp;
    private String status;

    public AppRequest() {
    }

    public AppRequest(String requestId, long timestamp, String status) {
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.status = status;
    }

    @DynamoDBHashKey(attributeName = "requestId")
    public String getRequestId() {
        return requestId;
    }

    public AppRequest setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    @DynamoDBAttribute(attributeName = "timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

    public AppRequest setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @DynamoDBAttribute(attributeName = "processedTimestamp")
    public Long getProcessedTimestamp() {
        return processedTimestamp;
    }

    public AppRequest setProcessedTimestamp(Long processedTimestamp) {
        this.processedTimestamp = processedTimestamp;
        return this;
    }

    @DynamoDBAttribute(attributeName = "archivedTimestamp")
    public Long getArchivedTimestamp() {
        return archivedTimestamp;
    }

    public AppRequest setArchivedTimestamp(Long archivedTimestamp) {
        this.archivedTimestamp = archivedTimestamp;
        return this;
    }

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return status;
    }

    public AppRequest setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "AppRequest{" +
                "requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                ", processedTimestamp=" + processedTimestamp +
                ", archivedTimestamp=" + archivedTimestamp +
                ", status='" + status + '\'' +
                '}';
    }

}
