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
    private static final String SERVER_URL1 = "http://localhost:8081"; // Change this to your server's URL
//    /api/schema-service/v1/liveness_check

    // Helper method to send a single HTTP request and check the response
    private int sendHttpRequest(String endpoint) throws IOException {
        URL url = new URL(SERVER_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        long currentTime = System.currentTimeMillis();
        int responseCode = connection.getResponseCode();
        long currentTime1 = System.currentTimeMillis();
        System.out.println(currentTime1 - currentTime);
        connection.disconnect();
        return responseCode;
    }

    @Test
    public void testMultipleHttpRequestsConcurrently() throws InterruptedException, ExecutionException {
        // Send 10 concurrent HTTP GET requests
        int numberOfRequests = 10000;

        // Create a list of CompletableFuture for concurrent execution
        CompletableFuture<Integer>[] futures = new CompletableFuture[numberOfRequests];

        // Initialize the CompletableFutures to send requests concurrently
        // Create an executor with a larger pool size
        ExecutorService executor = Executors.newFixedThreadPool(100); // Adjust the pool size as needed
        // Wait for all requests to finish and check the responses

        for (int i = 0; i < numberOfRequests; i++) {
            int finalI = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    return sendHttpRequest("/records");  // Change /path according to your API
                } catch (IOException e) {
                    return -1;  // Return an error code if the request fails
                }
            }, executor);

        }


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
            System.out.println("Total Failures:"+failureReports.size());
            // Optionally, throw an exception to fail the test if there were any failures
            // You could throw an AssertionError or any custom exception
            throw new AssertionError("Some assertions failed. See details above.");
        }
    }
}
