package org.guavaberry.retryer;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class LinearRetryerTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test(expected = NullPointerException.class)
    public void testNullBaseTimeout() throws Exception {
        new LinearRetryer<>(null, Duration.ZERO);
    }

    @Test(expected = NullPointerException.class)
    public void testNullBaseTimeout2() throws Exception {
        new LinearRetryer<>(null, Duration.ZERO, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testNullBaseTimeout3() throws Exception {
        new LinearRetryer<>(null, Duration.ZERO, 1, new DefaultRetryCondition<>());
    }

    @Test(expected = NullPointerException.class)
    public void testNullMaxTimeout() throws Exception {
        new LinearRetryer<>(Duration.ZERO, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullMaxTimeout2() throws Exception {
        new LinearRetryer<>(Duration.ZERO, null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testNullMaxTimeout3() throws Exception {
        new LinearRetryer<>(Duration.ZERO, null, 1, new DefaultRetryCondition<>());
    }

    @Test
    public void retryConstructors() {
        new LinearRetryer<>();
        new LinearRetryer<>(Duration.ZERO, Duration.ZERO);
        new LinearRetryer<>(Duration.ZERO, Duration.ZERO, 1);
        new LinearRetryer<>(Duration.ZERO, Duration.ZERO, 1, new DefaultRetryCondition<>());

    }

    @Test
    public void testWait() throws Exception {
        new LinearRetryer<>().wait(1);
    }

    @Test
    public void testCalculateTimeoutOnZeroDuration() throws Exception {
        assertEquals(Duration.ZERO, new LinearRetryer<>().calculateTimeout(100));
    }

    @Test
    public void testCalculateTimeoutLessThanMaxDuration() throws Exception {
        LinearRetryer retryer = new LinearRetryer<>(Duration.ofSeconds(5), Duration.ofSeconds(100));
        assertEquals(Duration.ofSeconds(15), retryer.calculateTimeout(3));
    }

    @Test
    public void testCalculateTimeoutGreaterThanMaxDuration() throws Exception {
        LinearRetryer retryer = new LinearRetryer<>(Duration.ofSeconds(5), Duration.ofSeconds(100));
        assertEquals(Duration.ofSeconds(100), retryer.calculateTimeout(29));
    }

    @Test
    public void testCalculateTimeoutSameAsMaxDuration() throws Exception {
        LinearRetryer retryer = new LinearRetryer<>(Duration.ofSeconds(5), Duration.ofSeconds(100));
        assertEquals(Duration.ofSeconds(100), retryer.calculateTimeout(20));
    }

    @Test
    public void testGetBaseTimeout() throws Exception {
        LinearRetryer retryer = new LinearRetryer<>(Duration.ofSeconds(100), Duration.ZERO);
        assertEquals(Duration.ofSeconds(100), retryer.getBaseTimeout());
    }

    @Test
    public void testGetMaxTimeout() throws Exception {
        LinearRetryer retryer = new LinearRetryer<>(Duration.ZERO, Duration.ofSeconds(100));
        assertEquals(Duration.ofSeconds(100), retryer.getMaxTimeout());
    }

    @Test
    public void testWaitWithInterruption() {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(() -> {
            new LinearRetryer<>(Duration.ofSeconds(100), Duration.ofSeconds(100)).wait(1);
        });
        executor.shutdownNow();
        assertTrue(executor.isShutdown());

    }
}