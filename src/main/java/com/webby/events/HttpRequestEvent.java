package com.webby.events;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpRequestEvent {

    private InputStream request;
    private OutputStream response;


    public InputStream getRequest() {
        return request;
    }

    public OutputStream getResponse() {
        return response;
    }

    public void setRequest(InputStream request) {
        this.request = request;
    }

    public void setResponse(OutputStream response) {
        this.response = response;
    }
}