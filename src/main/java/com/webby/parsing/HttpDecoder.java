package com.webby.parsing;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * HttpDecoder:
 * InputStreamReader -> bytes to characters ( decoded with certain Charset ( ascii ) )
 * BufferedReader    -> character stream to text
 */
public class HttpDecoder {

    public static String decode(final InputStream inputStream) {
        return readMessage(inputStream).stream()
                .flatMap(str -> str.stream())
                .collect(Collectors.joining(" "));
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
            return Optional.empty();
        }
    }
}