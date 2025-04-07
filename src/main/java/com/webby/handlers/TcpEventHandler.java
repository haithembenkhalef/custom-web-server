package com.webby.handlers;

import com.webby.limiters.RateLimiter;
import com.webby.server.EventWebServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class TcpEventHandler implements Runnable {

    private Selector selector;
    private ServerSocketChannel socketChannel;
    private Map<String, SocketChannel> connections;
    private Map<SocketChannel, String> reversedConnections;
    private Map<String, String> responseStore;

    private boolean running;
    private EventWebServer webServer;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(12);


    public TcpEventHandler(Selector selector, ServerSocketChannel socketChannel, EventWebServer webServer) {
        this.selector = selector;
        this.socketChannel = socketChannel;
        this.connections = new ConcurrentHashMap<>();
        this.reversedConnections = new ConcurrentHashMap<>();
        responseStore = new ConcurrentHashMap<>();
        this.webServer = webServer;
    }

    @Override
    public void run() {
        running = true;
        RateLimiter rateLimiter = new RateLimiter();
        while (running) {

            try {
                selector.select();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Set<SelectionKey> selKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selKeys.iterator();

            //iterate over the selected keys
            while (keyIterator.hasNext()) {
                SelectionKey myKey = keyIterator.next();
                if (myKey.isAcceptable()) {
                    SocketChannel client = null;
                    try {
                        if(!rateLimiter.isRateLimited())
                            client = socketChannel.accept();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (client != null) {
                        try {
                            client.configureBlocking(false);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        } catch (ClosedChannelException e) {
                            throw new RuntimeException(e);
                        }
                        String connectionId = UUID.randomUUID().toString();
                        this.registerConnection(connectionId, client);
                        System.out.println("Connection Accepted\n");
                    }

                }

                if (myKey.isReadable()) {
                    // Read from client
                    SocketChannel client = (SocketChannel) myKey.channel();
                    String connectionId = reversedConnections.get(client);
                    if (connectionId != null && !connectionId.isEmpty()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        try {
                            int bytesRead = client.read(buffer);

                            if (bytesRead == -1) {
                                System.out.println("Client disconnected: " + client.getRemoteAddress());
                                client.close();
                                continue;
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        buffer.flip();
                        String requestString = new String(buffer.array(), 0, buffer.limit());
                        String httpStringResponse = webServer.handleConnection(connectionId, requestString);
                        registerResponse(connectionId, httpStringResponse);
                        buffer.clear();
                    }
                }
                if (myKey.isWritable()) {

                    SocketChannel client = (SocketChannel) myKey.channel();
                    String connectionId = reversedConnections.get(client);
                    String response;
                    if ((response = responseStore.get(connectionId)) != null) {
                        try {
                            responseStore.remove(connectionId);
                            ByteBuffer sendAck = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
                            client.write(sendAck);
                            client.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                //once read, each key is removed from the operation.
                keyIterator.remove();
            }
        }
    }

    private void registerConnection(String id, SocketChannel client) {
        this.connections.put(id, client);
        this.reversedConnections.put(client, id);
    }

    private void registerResponse(String id, String request) {
        responseStore.put(id, request);
    }

    public SocketChannel getConnection(String id) {
        return this.connections.get(id);
    }

    public void shutdown() {
        System.out.println("Shutting down server...");
        running = false;
        try {
            selector.close();
            socketChannel.close();
        } catch (IOException e) {
            System.out.println("error shutdown server");
            System.exit(-1);
        }
    }
}
