package com.webby.server;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.webby.events.HttpRequestEvent;
import com.webby.handlers.HttpHandler;
import com.webby.handlers.HttpRequestEventHandler;
import com.webby.handlers.RequestRunner;
import com.webby.model.HttpMethod;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class WebServer {

    private final static int bufferSize = 1024;
    private final Map<String, RequestRunner> routes;
    private ServerSocket serverSocket;
    private Disruptor<HttpRequestEvent> disruptor;
    private HttpHandler handler;
    private boolean running;


    public WebServer(int port) throws IOException {
        this.routes = new HashMap<>();
        this.serverSocket = new ServerSocket(port);
        this.disruptor = new Disruptor<>(HttpRequestEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(new HttpRequestEventHandler());
    }

    public void start() throws IOException {
        this.handler = new com.webby.handlers.HttpHandler(routes);
        disruptor.start();
        running = true;
        while (running) {
            Socket clientConnection = serverSocket.accept();
            System.out.println("Connection received");
            handleConnection(clientConnection);
        }
    }

    public void addRoute(HttpMethod opCode, String route, RequestRunner runner) {
        routes.put(opCode.name().concat(route), runner);
    }

    /*
     * Capture each Request / Response lifecycle in a thread
     * executed on the threadPool.
     */
    private void handleConnection(Socket clientConnection) {
        RingBuffer<HttpRequestEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent((event, sequence, buffer) -> {
            try {
                event.setRequest(clientConnection.getInputStream());
                event.setResponse(clientConnection.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Time Received Request"+ Instant.now());

    }
}
