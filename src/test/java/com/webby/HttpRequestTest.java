package com.webby;



import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpRequestTest {

    private static final String SERVER_URL = "http://localhost:8888"; // Change this to your server's URL

    // Helper method to send a single HTTP request and check the response
    private int sendHttpRequest(String endpoint) throws IOException {
        URL url = new URL(SERVER_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(2000);  // 2 seconds timeout to establish the connection
//        connection.setReadTimeout(5000);     // 2 seconds timeout to read from the connection
        return connection.getResponseCode();
    }

    @Test
    public void testMultipleHttpRequestsConcurrently() throws InterruptedException, ExecutionException {
        // Send 10 concurrent HTTP GET requests
        int numberOfRequests = 1000;

        // Create a list of CompletableFuture for concurrent execution
        CompletableFuture<Integer>[] futures = new CompletableFuture[numberOfRequests];

        // Initialize the CompletableFutures to send requests concurrently
        // Create an executor with a larger pool size
        ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests+5); // Adjust the pool size as needed
        for (int i = 0; i < numberOfRequests; i++) {
            int finalI = i;
            Thread.sleep(1);
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    System.out.println("sent"+ finalI);
                    return sendHttpRequest("/records");  // Change /path according to your API
                } catch (IOException e) {
                    System.out.println("failed");
                    e.printStackTrace();
                    return -1;  // Return an error code if the request fails
                }
            }, executor);

        }

        // Wait for all requests to finish and check the responses
        CompletableFuture.allOf(futures).join();  // Wait for all requests to complete


        // List to collect failure reports
        List<String> failureReports = new ArrayList<>();

        // Check that all requests returned 200 OK
        // Iterate through the futures and assert
        for (int index = 0; index < futures.length; index++) {
            try {
                // Wait for the future result
                int responseCode = futures[index].get();

                // If it's not 200, record the failure but don't stop the test
                if (responseCode != 200) {
                    failureReports.add("Expected status for " + index + " to be 200 OK but got " + responseCode);
                }
            } catch (Exception e) {
                failureReports.add("Failed to get response for index " + index + ": " + e.getMessage());
            }
        }

        // After all futures have been processed, print the failure reports
        if (!failureReports.isEmpty()) {
            // Print all failed assertions
            System.out.println("Failures:");
            failureReports.forEach(System.out::println);
            System.out.println("Total Failures:"+failureReports.size());
            // Optionally, throw an exception to fail the test if there were any failures
            // You could throw an AssertionError or any custom exception
            throw new AssertionError("Some assertions failed. See details above.");
        }
    }
}
