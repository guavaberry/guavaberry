package org.guavaberry.retryer;

/**
 * The class contains the condition for deciding whether a retry should occur or not.
 *
 * @param <T> the return value type.
 */
public interface RetryCondition<T> {

    /**
     * Decides whether the retry should occur for the given exception.
     *
     * @param ex the exception to be evaluated.
     * @return true if retry should occur on exception ex.
     */
    default boolean shouldRetryOnException(Exception ex) {
        return true;
    }

    /**
     * Decides whether the retry should occur for the given returned value.
     *
     * @param returnValue the returned value to be evaluated.
     * @return true if the retry should occur on the returned value returnValue.
     */
    default boolean shouldRetryOnReturnValue(T returnValue) {
        return false;
    }
}
