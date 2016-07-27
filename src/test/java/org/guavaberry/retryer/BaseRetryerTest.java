package org.guavaberry.retryer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BaseRetryerTest {
    private TestRetryer retryer;
    private TestRetryer infiniteRetryer;

    private class TestRetryer<T> extends BaseRetryer<T> {
        private int sleepCount = 0;
        public TestRetryer(final int attempts,
                           final RetryCondition<T> retryCondition) {
            super(attempts, retryCondition);
        }

        public TestRetryer(final int attempts) {
            super(attempts);
        }

        public TestRetryer() {
            super();
        }
        @Override
        protected void wait(final int time) {
            sleepCount++;
        }
        public int getSleepCount() {
            return sleepCount;
        }
    }

    private class TestClass {
        private final int returnMaxCount;
        private final int exceptionMaxCount;
        private int returnCount = 0;
        private int exceptionCount = 0;

        public TestClass(int returnMaxCount, int exceptionMaxCount) {
            this.returnMaxCount = returnMaxCount;
            this.exceptionMaxCount = exceptionMaxCount;
        }

        public void raiseUntilCount() {
            exceptionCount++;
            if (exceptionCount < exceptionMaxCount) {
                throw new TestException();
            }
        }
        public boolean falseUntilCount() {
            returnCount++;
            if (returnCount < returnMaxCount) {
                return false;
            }
            return true;
        }

        public int getReturnCount() {
            return returnCount;
        }

        public int getExceptionCount() {
            return exceptionCount;
        }
    }

    public class TestException extends RuntimeException { }

    private class TestRetryCondition implements RetryCondition {
        @Override
        public boolean shouldRetryOnReturnValue(Object returnValue) {
            return ! (boolean) returnValue;
        }

        @Override
        public boolean shouldRetryOnException(Exception ex) {
            return ex instanceof TestException;
        }
    }

    @Before
    public void setUp() throws Exception {
        retryer = new TestRetryer(10, new TestRetryCondition());
        infiniteRetryer = new TestRetryer(-1, new TestRetryCondition());
    }

    @Test
    public void testConstructors() {
        new TestRetryer(10, new TestRetryCondition());
        new TestRetryer(10);
        new TestRetryer();
    }

    @Test(expected = IllegalArgumentException.class)
    public void retryNegativeTimes() throws Exception {
        new TestRetryer<>(-10);
    }

    @Test(expected = NullPointerException.class)
    public void retryNullCondition() throws Exception {
        new TestRetryer<>(10, null);
    }

    @Test
    public void retryConstructor() {
        new TestRetryer<>(-1, new DefaultRetryCondition<>());
    }

    @Test
    public void retryOnReturn() {
        TestClass myClass = new TestClass(5, 0);
        retryer.retry(() -> myClass.falseUntilCount());
        assertEquals(5, myClass.getReturnCount());
        assertEquals(0, myClass.getExceptionCount());
        assertEquals(4, retryer.getSleepCount());
    }

    @Test
    public void retryOnReturnInfiniteRetries() {
        TestClass myClass = new TestClass(5, 0);
        infiniteRetryer.retry(() -> myClass.falseUntilCount());
        assertEquals(5, myClass.getReturnCount());
        assertEquals(0, myClass.getExceptionCount());
        assertEquals(4, infiniteRetryer.getSleepCount());
    }

    @Test(expected = RetryException.class)
    public void retryOnReturnTooManyRetries() {
        TestClass myClass = new TestClass(11, 0);
        retryer.retry(() -> myClass.falseUntilCount());
    }

    @Test
    public void retryOnException() {
        TestClass myClass = new TestClass(0, 5);
        retryer.retry(() -> {
            myClass.raiseUntilCount();
            return myClass.falseUntilCount();
        });
        assertEquals(1, myClass.getReturnCount());
        assertEquals(5, myClass.getExceptionCount());
        assertEquals(4, retryer.getSleepCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void retryOnDifferentException() {
        TestClass myClass = new TestClass(0, 5);
        retryer.retry(() -> {
            throw new IllegalArgumentException();
        });
    }

    @Test
    public void retryOnExceptionInfiniteRetries() {
        TestClass myClass = new TestClass(0, 5);
        infiniteRetryer.retry(() -> {
            myClass.raiseUntilCount();
            return myClass.falseUntilCount();
        });
        assertEquals(1, myClass.getReturnCount());
        assertEquals(5, myClass.getExceptionCount());
        assertEquals(4, infiniteRetryer.getSleepCount());
    }

    @Test
    public void retryOnExceptionTooManyRetries() {
        try {
            TestClass myClass = new TestClass(0, 11);
            retryer.retry(() -> {
                myClass.raiseUntilCount();
                return myClass.falseUntilCount();
            });
        } catch (RetryException ex) {
            assertEquals(TestException.class, ex.getCause().getClass());
            assertEquals(10, ex.getAttempts());
            assertEquals(null, ex.getLastReturnValue());
            assertEquals(ex.getCause(), ex.getLastThrowable());
            assertEquals(9, retryer.getSleepCount());
        }
    }

    @Test
    public void testGetMaxAttempts() {
        assertEquals(10, retryer.getMaxAttempts());
        assertEquals(-1, infiniteRetryer.getMaxAttempts());
    }
}