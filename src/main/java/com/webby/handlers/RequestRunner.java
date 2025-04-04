package com.webby.handlers;

import com.webby.model.HttpRequest;
import com.webby.model.HttpResponse;

@FunctionalInterface
public interface RequestRunner {
    HttpResponse run(HttpRequest request);
}