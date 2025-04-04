package com.webby;

import com.webby.server.WebServer;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {

        new WebServer(8888).start();
    }
}
