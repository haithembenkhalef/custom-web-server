package com.webby;

import com.webby.model.HttpMethod;
import com.webby.model.HttpResponse;
import com.webby.server.EventWebServer;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {

        EventWebServer webServer = new EventWebServer(8888);

        //Controller
        webServer.handleRoute(HttpMethod.GET, "/records", (request -> {
            System.out.println("controller called");
            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .setEntity("Plain text entity")
                    .addHeader("Content-Type", "text/plain")
                    .build();
        }));
        webServer.start();

        /*WebServer webServer = new WebServer(8888);

        //Controller
        webServer.handleRoute(HttpMethod.GET, "/records", (request -> {
            System.out.println("controller called");
            return new HttpResponse.Builder()
                    .setStatusCode(200)
                    .setEntity("Plain text entity")
                    .addHeader("Content-Type", "text/plain")
                    .build();
        }));

        webServer.start();*/
    }
}
