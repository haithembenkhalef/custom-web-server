package com.webby.limiters;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    // Store request counts for each IP address (or user)
    private static final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_WINDOW = 100000; // Limit: 100 requests per minute
    private static final long WINDOW_DURATION = TimeUnit.MILLISECONDS.toMillis(1000); // 1 minute window
    RequestCounter counter = new RequestCounter(System.currentTimeMillis());


    public synchronized boolean isRateLimited() {

        long currentTime = System.currentTimeMillis();

        // Check if the window has expired
        if (currentTime - counter.windowStartTime > WINDOW_DURATION) {
            // Reset the counter for the new window
            counter.windowStartTime = currentTime;
            counter.requestCount = 0;
        }

        // Check if the request count exceeds the limit
        if (counter.requestCount >= MAX_REQUESTS_PER_WINDOW) {
            return true; // Rate-limited
        }

        // Otherwise, increment the request count
        counter.requestCount++;
        return false; // Not rate-limited

    }

    private static class RequestCounter {
        long windowStartTime;
        int requestCount;

        RequestCounter(long startTime) {
            this.windowStartTime = startTime;
            this.requestCount = 0;
        }
    }
}
