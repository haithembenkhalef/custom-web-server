package com.webby.handlers;

import com.lmax.disruptor.EventHandler;
import com.webby.events.HttpRequestEvent;
import com.webby.parsing.HttpDecoder;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

public class HttpRequestEventHandler implements EventHandler<HttpRequestEvent> {

    @Override
    public void onEvent(HttpRequestEvent httpRequestEvent, long l, boolean b) throws Exception {
        System.out.println("Received Event " + httpRequestEvent.getRequest().available());
        System.out.println(HttpDecoder.decode(httpRequestEvent.getRequest()));
        OutputStream response = httpRequestEvent.getResponse();
        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(response));
        writeResponse(bufferedWriter, null);

//
//        Optional<HttpRequest> request = HttpDecoder.decode(httpRequestEvent.getRequest());
//        request.ifPresentOrElse((r) -> handleRequest(r, bufferedWriter), () -> handleInvalidRequest(bufferedWriter));
//
        bufferedWriter.close();

        System.out.println("Time Handling Request"+ Instant.now());

    }

    /**
     * Write a HTTPResponse to an outputstream
     * @param outputStream - the outputstream
     * @param response - the HTTPResponse
     */
    public static void writeResponse(final BufferedWriter outputStream, final HttpResponse response) {
        try {
            final int statusCode = 200;
            final String statusCodeMeaning = "OK";
            final List<String> responseHeaders = List.of("");

            outputStream.write("HTTP/1.1 " + statusCode + " " + statusCodeMeaning + "\r\n");

            for (String header : responseHeaders) {
                outputStream.write(header);
            }

            outputStream.write("\r\n");

        } catch (Exception ignored) {
            System.out.println("error writing response");
        }
    }
}