package org.guavaberry.retryer;

import org.guavaberry.function.NullaryOperator;

import java.util.Objects;

/**
 * Contains the template method for the basic retry strategy.
 *
 * <p>{@link BaseRetryer#retry(NullaryOperator)} attempts to call the lambda function
 * a number of times according to the given {@link BaseRetryer#maxAttempts}.
 * The method will retry depending on either the last value returned or
 * the exception raised by the function.
 * The logic of deciding whether a retry should occur or not depend on
 * the given {@link RetryCondition} instance.
 *
 * <p>Between a retry and another the method will wait a certain amount
 * depending on the implementation of the {@link BaseRetryer#wait(int)} method.
 *
 * @author  Filippo Squillace
 *
 * @param <T> type of the returned value
 *
 * @see     ConstantRetryer
 * @since   0.1.0
 */
public abstract class BaseRetryer<T> implements Retryer<T> {
    /**
     * Infinite number of attempts.
     */
    protected static final int INFINITE_MAX_ATTEMPTS = -1;

    /**
     * Default number of attempts.
     */
    protected static final int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * Maximum number of attempts to perform.
     */
    private final int maxAttempts;

    /**
     * Contains the logic for deciding whether a retry should occur or not.
     */
    private final RetryCondition retryCondition;

    /**
     * Creates a {@link BaseRetryer} instance that retries until a
     * maximum attempts maxAttempts. It accepts a {@link RetryCondition} for deciding
     * whether the retry should occur depending on either the returned value or
     * the exception raised by the lambda function.
     * If maxAttempts is -1 the instance will attempt to retry indefinitely until {@link RetryCondition} has
     * satisfied.
     *
     * @param maxAttempts maximum number of attempts to perform. If value is -1 it will retry indefinitely.
     * @param retryCondition contains the logic for deciding whether a retry should occur or not.
     *
     * @throws NullPointerException if retryCondition is null.
     * @throws IllegalArgumentException if maxAttempts is less than -1.
     */
    public BaseRetryer(final int maxAttempts,
                       final RetryCondition<T> retryCondition) {
        if (maxAttempts < INFINITE_MAX_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be either -1"
                                               + " or any other non-negative int.");
        }
        this.maxAttempts = maxAttempts;
        this.retryCondition = Objects.requireNonNull(retryCondition);
    }

    /**
     * Create a {@link BaseRetryer} with a {@link DefaultRetryCondition}.
     *
     * @param maxAttempts maximum number of attempts to perform. If value is -1 it will retry indefinitely.
     *
     * @throws IllegalArgumentException if maxAttempts is less than -1.
     */
    public BaseRetryer(final int maxAttempts) {
        this(maxAttempts, new DefaultRetryCondition<>());
    }

    /**
     * Create a {@link BaseRetryer} with a {@link DefaultRetryCondition}
     * and a maximum number of retries equals to DEFAULT_MAX_ATTEMPTS.
     */
    public BaseRetryer() {
        this(DEFAULT_MAX_ATTEMPTS, new DefaultRetryCondition<>());
    }

    /**
     * Method invoked by retry for waiting between the attempts.
     * This method must be implemented by the concrete class.
     *
     * @param attempt the number of attempt done so far.
     */
    protected abstract void wait(final int attempt);

    /**
     * Attempts to call the lambda function operator a number of times
     * depending on maxAttempts and the {@link RetryCondition} logic.
     *
     * @param operator the lambda function to call.
     *
     * @return the value returned by the function.
     *
     * @throws RetryException if the number of attempts exceeds the maximum value.
     */
    @Override
    public final T retry(final NullaryOperator<T> operator) {
        int attempt = 0;
        T lastReturnValue = null;
        Exception lastException = null;

        while (isRetryNeeded(attempt)) {
            try {
                lastReturnValue = operator.op();
                if (!retryCondition.shouldRetryOnReturnValue(lastReturnValue)) {
                    return lastReturnValue;
                }
            } catch (Exception ex) {
                lastException = ex;
                if (!retryCondition.shouldRetryOnException(ex)) {
                    throw ex;
                }
            }
            // Increment attempt only on finite retries
            if (maxAttempts > INFINITE_MAX_ATTEMPTS) {
                attempt++;
            }
            // Do not sleep if no retries are available
            if (isRetryNeeded(attempt)) {
                wait(attempt);
            }
        }
        throw new RetryException("Exceeded number of retries",
                                   maxAttempts, lastReturnValue, lastException);
    }

    /**
     * Decides whether a retry occurs depending on the number of attempts performed so far.
     *
     * @param attempt the number of attempts done so far.
     *
     * @return true if retry is needed.
     */
    private boolean isRetryNeeded(final int attempt) {
        return attempt < maxAttempts || maxAttempts == INFINITE_MAX_ATTEMPTS;
    }

    /**
     * @return the maximum number of attempts.
     */
    public final int getMaxAttempts() {
        return maxAttempts;
    }
}
