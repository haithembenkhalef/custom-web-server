package com.webby.parsing;

import com.webby.model.HttpMethod;
import com.webby.model.HttpRequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * HttpDecoder:
 * InputStreamReader -> bytes to characters ( decoded with certain Charset ( ascii ) )
 * BufferedReader    -> character stream to text
 */
public class HttpDecoder {

    public static Optional<List<String>> decode(final InputStream inputStream) {
        return readMessage(inputStream);
    }

    public static Optional<HttpRequest> buildRequest(final InputStream inputStream) {
        return decode(inputStream).flatMap(HttpDecoder::buildRequest);
    }

    private static Optional<List<String>> readMessage(final InputStream inputStream) {
        try {
            if (!(inputStream.available() > 0)) {
                return Optional.empty();
            }

            final char[] inBuffer = new char[inputStream.available()];
            final InputStreamReader inReader = new InputStreamReader(inputStream);
            final int read = inReader.read(inBuffer);

            List<String> message = new ArrayList<>();

            try (Scanner sc = new Scanner(new String(inBuffer))) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    message.add(line);
                }
            }

            return Optional.of(message);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return Optional.empty();
        }
    }

    private static Optional<com.webby.model.HttpRequest> buildRequest(List<String> message) {
        if (message.isEmpty()) {
            return Optional.empty();
        }

        String firstLine = message.get(0);
        String[] httpInfo = firstLine.split(" ");

        if (httpInfo.length != 3) {
            return Optional.empty();
        }

        String protocolVersion = httpInfo[2];
        if (!protocolVersion.equals("HTTP/1.1")) {
            return Optional.empty();
        }

        try {
            com.webby.model.HttpRequest.Builder requestBuilder = new com.webby.model.HttpRequest.Builder();
            requestBuilder.setHttpMethod(HttpMethod.valueOf(httpInfo[0]));
            requestBuilder.setUri(new URI(httpInfo[1]));
            return Optional.of(addRequestHeaders(message, requestBuilder));
        } catch (URISyntaxException | IllegalArgumentException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static HttpRequest addRequestHeaders(final List<String> message, final HttpRequest.Builder builder) {
        final Map<String, List<String>> requestHeaders = new HashMap<>();

        if (message.size() > 1) {
            for (int i = 1; i < message.size(); i++) {
                String header = message.get(i);
                int colonIndex = header.indexOf(':');

                if (! (colonIndex > 0 && header.length() > colonIndex + 1)) {
                    break;
                }

                String headerName = header.substring(0, colonIndex);
                String headerValue = header.substring(colonIndex + 1);

                requestHeaders.compute(headerName, (key, values) -> {
                    if (values != null) {
                        values.add(headerValue);
                    } else {
                        values = new ArrayList<>();
                    }
                    return values;
                });
            }
        }

        builder.setRequestHeaders(requestHeaders);
        return builder.build();
    }
}