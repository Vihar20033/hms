package com.hms.common.concurrency;

import java.util.concurrent.TimeUnit;

public interface LockService {
    /**
     * Tries to acquire a lock.
     * @param lockKey Unique key for the lock
     * @param waitTime Time to wait for the lock
     * @param leaseTime Time to hold the lock before automatic release
     * @param unit Time unit
     * @return true if acquired, false otherwise
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * Releases the lock.
     * @param lockKey Unique key for the lock
     */
    void unlock(String lockKey);
}
