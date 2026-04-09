package com.hms.common.concurrency;

public interface CounterService {
    /**
     * Increments the counter and returns the next value.
     * @param key Unique key for the counter
     * @return The next value
     */
    long increment(String key);
}
