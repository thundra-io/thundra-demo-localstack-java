package io.thundra.demo.localstack.model;

public class Message {
    private String requestId;

    public Message() {
    }

    public Message(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
