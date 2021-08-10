package io.thundra.demo.localstack.model;

public class AppRequests {
    private String id;
    private String requestId;
    private String timestamp;
    private String status;

    public AppRequests() {
    }

    public AppRequests(String id, String requestId, String timestamp, String status) {
        this.id = id;
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AppRequests{" +
                "id=" + id +
                ", requestId='" + requestId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
