package org.guavaberry.retryer;

import org.guavaberry.function.NullaryOperator;

/**
 * Defines the interface for the retryer. A retryer is an instance
 * that attempts to call a certain method (i.e. a lambda function)
 * and retries until a certain condition is no longer satisfied.
 *
 * <p>The retryer will work as a proxy for the method that need to be called
 * and, once completed, returns the value returned by method itself.
 * Useful for factory instance creation.
 *
 * <p>Suppose to have a class A with an method called myMethod() that returns
 * a boolean, the code would be the following:
 * {@code
 * A myObject = new A();
 * Retryer retryer = ... // any implementation of Retryer
 * boolean outcome = retryer.retry(()-> myObject.myMethod());
 * }
 *
 * @param <T> type of the instance to return.
 *
 * @see BaseRetryer
 */
public interface Retryer<T> {
    /**
     * Attempts to call operator multiple times.
     *
     * @param operator the method to be called.
     * @return the value returned by operator.
     */
    T retry(final NullaryOperator<T> operator);
}
