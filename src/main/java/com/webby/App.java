package com.webby;

import com.webby.model.HttpMethod;
import com.webby.model.HttpResponse;
import com.webby.server.WebServer;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {

        WebServer webServer = new WebServer(8888);

        WebServer webServer1 = new WebServer(8889);

        //Controller
        webServer.handleRoute(HttpMethod.GET, "/records", (request -> {
            System.out.println("controller implemented");
            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .setEntity("Plain text entity")
                    .addHeader("Content-Type", "text/plain")
                    .build();
        }));

        webServer1.handleRoute(HttpMethod.GET, "/records", (request -> {
            System.out.println("controller implemented");
            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .setEntity("Plain text entity")
                    .addHeader("Content-Type", "text/plain")
                    .build();
        }));

        webServer.start();
        webServer1.start();
    }
}
