package com.webby.handlers;

import com.lmax.disruptor.EventHandler;
import com.webby.events.HttpRequestEvent;
import com.webby.model.HttpRequest;
import com.webby.parsing.HttpDecoder;
import com.webby.parsing.ResponseWriter;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;

public class HttpRequestEventHandler implements EventHandler<HttpRequestEvent> {

    private final HttpHandler handler;

    public HttpRequestEventHandler(HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onEvent(HttpRequestEvent httpRequestEvent, long l, boolean b) throws Exception {
        System.out.println(Thread.currentThread().getName());
        InputStream streamRequest = httpRequestEvent.getRequest();
        OutputStream streamResponse = httpRequestEvent.getResponse();
        Optional<HttpRequest> httpRequest = HttpDecoder.buildRequest(streamRequest);
        com.webby.model.HttpResponse httpResponse = httpRequest.map(handler::handleRequest).orElseGet(handler::handleBadRequest);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(streamResponse));
        ResponseWriter.writeResponse(bufferedWriter, httpResponse);
        bufferedWriter.close();
        streamRequest.close();
    }

}
