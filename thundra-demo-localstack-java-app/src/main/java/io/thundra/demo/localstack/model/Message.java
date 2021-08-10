package io.thundra.demo.localstack.model;

public class Message {
    private String requestID;

    public Message() {
    }

    public Message(String requestID) {
        this.requestID = requestID;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }
}
