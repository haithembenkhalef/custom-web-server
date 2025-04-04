package com.webby.handlers;

import java.io.*;
import java.util.Map;

/**
 * Handle HTTP Request Response lifecycle.
 */
public class HttpHandler {

    private final Map<String, RequestRunner> routes;

    public HttpHandler(final Map<String, RequestRunner> routes) {
        this.routes = routes;
    }


    public void handleConnection(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        /*final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

        Optional<HttpRequest> request = HttpDecoder.decode(inputStream);
        request.ifPresentOrElse((r) -> handleRequest(r, bufferedWriter), () -> handleInvalidRequest(bufferedWriter));

        bufferedWriter.close();
        inputStream.close();*/
    }

}