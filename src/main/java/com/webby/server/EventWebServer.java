package com.webby.server;

import com.webby.handlers.HttpHandler;
import com.webby.handlers.RequestRunner;
import com.webby.handlers.TcpEventHandler;
import com.webby.model.HttpMethod;
import com.webby.model.HttpRequest;
import com.webby.model.HttpResponse;
import com.webby.parsing.HttpDecoder;
import com.webby.parsing.ResponseWriter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventWebServer implements IWebServer {

    private final Map<String, RequestRunner> routes;
    private Selector selector;
    ServerSocketChannel serverChannel;
    int port;
    private HttpHandler handler;
    private boolean running;
    private TcpEventHandler tcpEventHandler;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public EventWebServer(int port) throws IOException {
        this.port = port;
        this.routes = new ConcurrentHashMap<>();
        this.handler = new HttpHandler(routes);
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.tcpEventHandler = new TcpEventHandler(selector, serverChannel, this);
    }

    @Override
    public void start() {
        try {
            serverChannel.bind(new InetSocketAddress(port), 100000);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            new Thread(tcpEventHandler).start();
            System.out.println("Server started on port 8080...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        this.tcpEventHandler.shutdown();
    }

    public void handleRoute(HttpMethod opCode, String route, RequestRunner runner) {
        routes.put(opCode.name().concat(route), runner);
    }

    public String handleConnection(String id, String httpRequestString) {
        Optional<HttpRequest> httpRequest = HttpDecoder.buildRequest(httpRequestString);
        HttpRequest request = null;
        HttpResponse httpResponse;
        if (httpRequest.isPresent()) {
            request = httpRequest.get();
            httpResponse = handler.handleRequest(request);
        } else {
            httpResponse = handler.handleBadRequest();
        }
        try {
            String response = ResponseWriter.writeResponse(request, httpResponse);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
