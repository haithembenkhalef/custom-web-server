package com.webby.events;

import com.lmax.disruptor.*;
public class HttpRequestEventFactory implements EventFactory<HttpRequestEvent> {

    @Override
    public HttpRequestEvent newInstance() {
        return new HttpRequestEvent();
    }
}