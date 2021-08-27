package io.thundra.demo.localstack.model;

/**
 * @author tolga
 */
public class Response {

    private String requestId;
    private String status;

    public Response() {
    }

    public Response(String requestId, String status) {
        this.requestId = requestId;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
