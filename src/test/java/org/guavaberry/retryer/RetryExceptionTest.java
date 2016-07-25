package org.guavaberry.retryer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RetryExceptionTest {
    private RetryException exception;
    @Before
    public void setUp() {
       exception = new RetryException("error", 10, false, new RuntimeException("bad function"));
    }

    @Test
    public void getAttempts() throws Exception {
        assertEquals(10, exception.getAttempts());
    }

    @Test
    public void getLastReturnValue() throws Exception {
        assertEquals(false, exception.getLastReturnValue());
    }

    @Test
    public void getLastThrowable() throws Exception {
        assertEquals("bad function", exception.getLastThrowable().getMessage());
    }

    @Test
    public void getMessage() throws Exception {
        assertEquals("error: maxAttempts=10, lastReturnValue=false"
                   + ", lastException=java.lang.RuntimeException: bad function",
                     exception.getMessage());
    }

}