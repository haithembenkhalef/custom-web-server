package com.webby.parsing;

import com.webby.model.HttpRequest;
import com.webby.model.HttpResponse;
import com.webby.model.HttpStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResponseWriter {

    /**
     * Write a HTTPResponse to an outputstream
     * @param outputStream - the outputstream
     * @param response - the HTTPResponse
     */
    public static void writeResponse(final BufferedWriter outputStream, final HttpResponse response) throws IOException {

            final int statusCode = response.getStatusCode();
            final String statusCodeMeaning = HttpStatus.getStatusMessage(statusCode);
            final List<String> responseHeaders = buildHeaderStrings(response.getResponseHeaders());

            outputStream.write("HTTP/1.1 " + statusCode + " " + statusCodeMeaning + "\r\n");

            for (String header : responseHeaders) {
                outputStream.write(header);
            }

            outputStream.write("Connection: close\r\n");

            final Optional<String> entityString = response.getEntity().flatMap(ResponseWriter::getResponseString);
            if (entityString.isPresent()) {
                final String encodedString = new String(entityString.get().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                outputStream.write("Content-Length: " + encodedString.getBytes().length + "\r\n");
                outputStream.write("\r\n");
                outputStream.write(encodedString);
            } else {
                outputStream.write("\r\n");
            }
    }

    private static List<String> buildHeaderStrings(final Map<String, List<String>> responseHeaders) {
        final List<String> responseHeadersList = new ArrayList<>();

        responseHeaders.forEach((name, values) -> {
            final StringBuilder valuesCombined = new StringBuilder();
            values.forEach(valuesCombined::append);
            valuesCombined.append(";");

            responseHeadersList.add(name + ": " + valuesCombined + "\r\n");
        });

        return responseHeadersList;
    }

    public static void writeResponse(final HttpRequest request, final SocketChannel outputStream, final HttpResponse response) throws IOException {
        final int statusCode = response.getStatusCode();
        final String statusCodeMeaning = HttpStatus.getStatusMessage(statusCode);
        final Optional<String> entityString = response.getEntity().flatMap(ResponseWriter::getResponseString);

        StringBuilder responseBuilder = new StringBuilder();

        // Status line
        responseBuilder.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusCodeMeaning).append("\r\n");

        // Headers
        final Map<String, List<String>> headers = response.getResponseHeaders();

        // Automatically add Content-Length if entity is present
        if (entityString.isPresent()) {
            byte[] bodyBytes = entityString.get().getBytes(StandardCharsets.UTF_8);
            headers.put("Content-Length", List.of(String.valueOf(bodyBytes.length)));
            headers.putIfAbsent("Content-Type", List.of("text/plain")); // Optional fallback
        }

        // Add headers
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerLine = entry.getKey() + ": " + String.join(", ", entry.getValue());
            responseBuilder.append(headerLine).append("\r\n");
        }

        // Add connection close
        if(request!= null && request.getRequestHeaders().containsKey("Connection")) {
            List<String> connectionHeader = request.getRequestHeaders().get("Connection");
            if(!connectionHeader.contains("keep-alive"))
                responseBuilder.append("Connection: close\r\n");
            else responseBuilder.append("Connection: keep-alive\r\n");
        }

        // Empty line to end headers
        responseBuilder.append("\r\n");

        // Append body if present
        entityString.ifPresent(responseBuilder::append);

        // Convert to bytes
        byte[] responseBytes = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);

        // Write fully to channel
        writeToChannel(outputStream, ByteBuffer.wrap(responseBytes));
    }

    private static void writeToChannel(SocketChannel outputStream, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            outputStream.write(buffer);
        }
    }


    private static Optional<String> getResponseString(final Object entity) {
        // Currently only supporting Strings
        if (entity instanceof String) {
            try {
                return Optional.of(entity.toString());
            } catch (Exception ignored) {
            }
        }
        return Optional.empty();
    }
}
