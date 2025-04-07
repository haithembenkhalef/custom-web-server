//package com.webby.handlers;
//
//import com.lmax.disruptor.EventHandler;
//import com.webby.events.HttpRequestEvent;
//import com.webby.model.HttpRequest;
//import com.webby.model.HttpResponse;
//import com.webby.parsing.HttpDecoder;
//import com.webby.parsing.ResponseWriter;
//import com.webby.server.EventWebServer;
//
//import java.io.*;
//import java.net.Socket;
//import java.nio.channels.SocketChannel;
//import java.util.Optional;
//
//public class HttpRequestEventHandler implements EventHandler<HttpRequestEvent> {
//
//    private final HttpHandler handler;
//
//    private static final String httpProtocolV1 = "HTTP/1.1";
//    private final EventWebServer webServer;
//
//
//    public HttpRequestEventHandler(HttpHandler handler, EventWebServer webServer) {
//        this.handler = handler;
//        this.webServer = webServer;
//    }
//
//    @Override
//    public void onEvent(HttpRequestEvent httpRequestEvent, long l, boolean b) throws Exception {
//        new Thread(() -> {
//            String connectionId = httpRequestEvent.getConnectionId();
//            String httpRequestString = httpRequestEvent.getHttpRequestString();
//            Optional<HttpRequest> httpRequest = HttpDecoder.buildRequest(httpRequestString);
//            HttpRequest request = null;
//            HttpResponse response;
//            if (httpRequest.isPresent()) {
//                request = httpRequest.get();
//                response = handler.handleRequest(request);
//            } else {
//                response = handler.handleBadRequest();
//            }
//            SocketChannel client = webServer.getConnection(connectionId);
//            assert client.isConnected();
//            try {
//                ResponseWriter.writeResponse(request, client, response);
//            } catch (IOException e) {
//                System.out.println("error processing request" + connectionId);
//            }
//        }).start();
//
//        // Capture copies
//        /*CompletableFuture
//                .supplyAsync(() -> HttpDecoder.buildRequest(streamRequest))
//                .thenApply(opt -> opt.map(handler::handleRequest).orElseGet(handler::handleBadRequest))
//                .thenApply(response -> {
//                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(streamResponse));
//                    try {
//                        ResponseWriter.writeResponse(writer, response);
//                        writer.flush();
//                        return writer;
//                    } catch (IOException ignored) {
//                        return null;
//                    }
//                }).thenAccept(bufferedWriter -> {
//                    try {
//                        bufferedWriter.close();
//                        this.close(httpRequestEvent);
//                    } catch (IOException e) {
//                        System.out.println(e.getMessage());
//                        this.close(httpRequestEvent);
//                    }
//                });*/
//
//    }
//
//    private static boolean isProcessed(HttpRequestEvent httpRequestEvent) {
//        return httpRequestEvent.isProcessed();
//    }
//
//    private static boolean validateStreams(InputStream streamRequest, OutputStream streamResponse) throws IOException {
//        return streamRequest != null && streamRequest.available() > 0 && streamResponse != null;
//    }
//
//    private static boolean validateConnection(Socket httpConnection) {
//        return httpConnection != null && httpConnection.isConnected();
//
//    }
//
////    private void close(HttpRequestEvent requestEvent) {
////        try {
////            this.server.closeConnection(requestEvent.getConnectionId());
////        } catch (IOException e) {
////            System.out.println(e.getMessage());
////            throw new RuntimeException(e);
////        }
////    }
//
//}
