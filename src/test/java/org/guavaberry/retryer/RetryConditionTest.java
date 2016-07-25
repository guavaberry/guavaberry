package org.guavaberry.retryer;

import org.junit.Test;

import static org.junit.Assert.*;

public class RetryConditionTest {
    private class TestRetryCondition implements RetryCondition {}

    @Test
    public void shouldRetryOnException() throws Exception {
        assertTrue(new TestRetryCondition().shouldRetryOnException(new Exception()));
    }

    @Test
    public void shouldRetryOnReturnValue() throws Exception {
        assertFalse(new TestRetryCondition().shouldRetryOnReturnValue(10));
    }

}