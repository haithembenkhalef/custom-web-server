package com.webby.server;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.webby.events.HttpRequestEvent;
import com.webby.events.HttpRequestEventFactory;
import com.webby.handlers.HttpHandler;
import com.webby.handlers.HttpRequestEventHandler;
import com.webby.handlers.RequestRunner;
import com.webby.model.HttpMethod;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {

    private final static int bufferSize = 1024;
    private final Map<String, RequestRunner> routes;
    private final ServerSocket serverSocket;
    private final Disruptor<HttpRequestEvent> disruptor;
    private HttpHandler handler;
    private boolean running;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public WebServer(int port) throws IOException {
        this.routes = new HashMap<>();
        this.serverSocket = new ServerSocket(port);
        this.disruptor = new Disruptor<>(new HttpRequestEventFactory(), bufferSize, DaemonThreadFactory.INSTANCE);
        this.handler = new com.webby.handlers.HttpHandler(routes);
        disruptor.handleEventsWith(new HttpRequestEventHandler(handler));
    }

    public void start() throws IOException {
        if (!running) {
            executorService.submit(() -> {
                running = true;
                disruptor.start();
                while (running) {
                    Socket clientConnection = null;
                    try {
                        clientConnection = serverSocket.accept();
                        System.out.println("Connection received");
                    } catch (IOException e) {
                        e.printStackTrace();
                        running = false;
                        throw new RuntimeException(e);
                    }
                    handleConnection(clientConnection);
                }
            });
        }
    }

    public void stop() throws IOException {
        running = false;
        executorService.shutdown();
        disruptor.shutdown();
        serverSocket.close();
    }

    public void handleRoute(HttpMethod opCode, String route, RequestRunner runner) {
        routes.put(opCode.name().concat(route), runner);
    }

    private void handleConnection(Socket clientConnection) {
        System.out.println(Thread.currentThread().getName());
        RingBuffer<HttpRequestEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent((event, sequence, buffer) -> {
            try {
                event.setRequest(clientConnection.getInputStream());
                event.setResponse(clientConnection.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }
}
