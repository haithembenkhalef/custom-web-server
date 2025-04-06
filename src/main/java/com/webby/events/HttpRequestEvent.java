package com.webby.events;

public class HttpRequestEvent {

    String connectionId;

    String httpRequestString;

    public String getHttpRequestString() {
        return httpRequestString;
    }

    public void setHttpRequestString(String httpRequestString) {
        this.httpRequestString = httpRequestString;
    }

    private boolean isProcessed = false;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void reset() {
        this.isProcessed = false;
        this.setHttpRequestString("");
    }
}