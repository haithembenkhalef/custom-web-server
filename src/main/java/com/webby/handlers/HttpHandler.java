package com.webby.handlers;

import com.webby.model.HttpRequest;
import com.webby.model.HttpResponse;
import com.webby.model.HttpStatus;

import java.util.Map;

/**
 * Handle HTTP Request Response lifecycle.
 */
public class HttpHandler {

    private final Map<String, RequestRunner> routes;

    private static final HttpResponse INVALID_REQUEST_RESPONSE =
            new HttpResponse.Builder()
                    .setStatusCode(HttpStatus.NOT_FOUND)
                    .setEntity(HttpStatus.getStatusMessage(HttpStatus.NOT_FOUND))
                    .build();

    private static final HttpResponse BAD_REQUEST_RESPONSE =
            new HttpResponse.Builder()
                    .setStatusCode(HttpStatus.BAD_REQUEST)
                    .setEntity(HttpStatus.getStatusMessage(HttpStatus.BAD_REQUEST))
                    .build();

    public HttpHandler(final Map<String, RequestRunner> routes) {
        this.routes = routes;
    }

    public HttpResponse handleRequest(HttpRequest httpRequest) {
        final String routeKey = httpRequest.getHttpMethod().name().concat(httpRequest.getUri().getRawPath());
        if(routes.containsKey(routeKey)) {
            return routes.get(routeKey).run(httpRequest);
        }
        return handleInvalidRequest();
    }

    public HttpResponse handleBadRequest() {
        return BAD_REQUEST_RESPONSE;
    }

    private HttpResponse handleInvalidRequest() {
        return INVALID_REQUEST_RESPONSE;
    }

}