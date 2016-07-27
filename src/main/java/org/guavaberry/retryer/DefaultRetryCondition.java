package org.guavaberry.retryer;

/**
 * The default retry condition that attempts to retry only whenever any exception is raised.
 *
 * @param <T> type of the return value.
 */
public class DefaultRetryCondition<T> implements RetryCondition<T> {
}
