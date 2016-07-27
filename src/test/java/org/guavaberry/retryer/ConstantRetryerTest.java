package org.guavaberry.retryer;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class ConstantRetryerTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test(expected = NullPointerException.class)
    public void testNullTimeout() throws Exception {
        new ConstantRetryer<>(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullTimeout2() throws Exception {
        new ConstantRetryer<>(null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testNullTimeout3() throws Exception {
        new ConstantRetryer<>(null, 1, new DefaultRetryCondition<>());
    }

    @Test
    public void retryConstructors() {
        new ConstantRetryer<>();
        new ConstantRetryer<>(Duration.ZERO);
        new ConstantRetryer<>(Duration.ZERO, 1);
        new ConstantRetryer<>(Duration.ZERO, 1, new DefaultRetryCondition<>());

    }

    @Test
    public void testWait() throws Exception {
        new ConstantRetryer<>().wait(1);
    }

    @Test
    public void testGetTimeout() throws Exception {
        ConstantRetryer retryer = new ConstantRetryer<>(Duration.ofSeconds(100));
        assertEquals(Duration.ofSeconds(100), retryer.getTimeout());
    }

    @Test
    public void testWaitWithInterruption() {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(() -> {
                new ConstantRetryer<>(Duration.ofSeconds(100)).wait(1);
        });
        executor.shutdownNow();
        assertTrue(executor.isShutdown());

    }
}