package io.thundra.demo.localstack.model;

public class Response {
    private String requestID;
    private String status;

    public Response() {
    }

    public Response(String requestID, String status) {
        this.requestID = requestID;
        this.status = status;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
