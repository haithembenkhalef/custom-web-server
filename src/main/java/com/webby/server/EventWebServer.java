package com.webby.server;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.webby.events.HttpRequestEvent;
import com.webby.events.HttpRequestEventFactory;
import com.webby.handlers.HttpHandler;
import com.webby.handlers.HttpRequestEventHandler;
import com.webby.handlers.RequestRunner;
import com.webby.handlers.TcpEventHandler;
import com.webby.model.HttpMethod;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventWebServer implements IWebServer {

    private final static int bufferSize = 16384;
    private final Map<String, RequestRunner> routes;
    private final Disruptor<HttpRequestEvent> disruptor;
    private Selector selector;
    ServerSocketChannel serverChannel;
    int port;
    private HttpHandler handler;
    private boolean running;
    private TcpEventHandler tcpEventHandler;


    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public EventWebServer(int port) throws IOException {
        this.routes = new ConcurrentHashMap<>();
        this.disruptor = new Disruptor<>(new HttpRequestEventFactory(), bufferSize, DaemonThreadFactory.INSTANCE);
        this.handler = new HttpHandler(routes);
        disruptor.handleEventsWith(new HttpRequestEventHandler(handler, this));
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        this.port = port;
        this.tcpEventHandler = new TcpEventHandler(selector, serverChannel, this);
    }

    @Override
    public void start() {
        try {
            this.disruptor.start();
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started on port 8080...");
            new Thread(tcpEventHandler).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void stop() {

    }

    public void handleRoute(HttpMethod opCode, String route, RequestRunner runner) {
        routes.put(opCode.name().concat(route), runner);
    }

    public SocketChannel getConnection(String id) {
        return tcpEventHandler.getConnection(id);
    }

    public void handleConnection(String id, String msg) {
        RingBuffer<HttpRequestEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent((event, sequence, buffer) -> {
            event.reset();
            event.setHttpRequestString(msg);
            event.setConnectionId(id);
        });
    }
}
