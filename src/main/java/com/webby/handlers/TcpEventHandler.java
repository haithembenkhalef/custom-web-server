package com.webby.handlers;

import com.webby.server.EventWebServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TcpEventHandler implements Runnable {

    private Selector selector;
    private ServerSocketChannel socketChannel;
    private Map<String, SocketChannel> connections;
    private Map<SocketChannel, String> reversedConnections;
    private boolean running;
    private EventWebServer webServer;

    public TcpEventHandler(Selector selector, ServerSocketChannel socketChannel, EventWebServer webServer) {
        this.selector = selector;
        this.socketChannel = socketChannel;
        this.connections = new ConcurrentHashMap<>();
        this.reversedConnections = new ConcurrentHashMap<>();
        this.webServer = webServer;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                System.out.println("I'm a server and I'm waiting for new connection and buffer select...");

                selector.select();

                //define a set of selectable keys
                Set<SelectionKey> selKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selKeys.iterator();

                //iterate over the selected keys
                while(keyIterator.hasNext()) {
                    SelectionKey myKey = keyIterator.next();
                    if(myKey.isAcceptable()) {
                        SocketChannel client = socketChannel.accept();
                        if(client != null) {
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                            String connectionId = UUID.randomUUID().toString();
                            this.registerConnection(connectionId, client);
                            System.out.println("Connection Accepted: " + client.getLocalAddress() + "\n");
                        }

                    }

                    if(myKey.isReadable()) {
                        // Read from client
                        SocketChannel client = (SocketChannel) myKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int bytesRead = client.read(buffer);

                        if (bytesRead == -1) {
                            System.out.println("Client disconnected: " + client.getRemoteAddress());
                            client.close();
                            continue;
                        }

                        buffer.flip();
                        String msg = new String(buffer.array(), 0, buffer.limit());
                        System.out.println("Received: " + msg);
                        String connectionId = reversedConnections.get(client);
                        if (connectionId != null && !connectionId.isEmpty() && !msg.isEmpty()) {
                            webServer.handleConnection(connectionId, msg);
                        }

                    }
                }
                //once read, each key is removed from the operation.
                keyIterator.remove();
            } catch (IOException e) {
                System.out.println("error on server thread.");
            }
        }
    }

    private void registerConnection(String id, SocketChannel client) {
        this.connections.put(id, client);
        this.reversedConnections.put(client, id);
    }

    public SocketChannel getConnection(String id) {
        return this.connections.get(id);
    }
}
