package org.guavaberry.retryer;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link Retryer} that times out a constant amount of time between retries.
 *
 * @param <T> the type of the return value.
 */
public class ConstantRetryer<T> extends BaseRetryer<T> {
    /**
     * The amount of time to wait between retries.
     */
    private final Duration timeout;

    /**
     * Creates a {@link ConstantRetryer} instance that retries until a
     * maximum attempts maxAttempts. It accepts a {@link RetryCondition} for deciding
     * whether the retry should occur depending on either the returned value or
     * the exception raised by the lambda function.
     * If maxAttempts is -1 the instance will attempt to retry indefinitely until {@link RetryCondition} has
     * satisfied.
     *
     * <p>This implementation waits a constant amount of time defined by timeout between retries.
     *
     * @param timeout the amount of time to wait between retries.
     * @param maxAttempts maximum number of attempts to perform. If value is -1 it will retry indefinitely.
     * @param retryCondition contains the logic for deciding whether a retry should occur or not.
     *
     * @throws NullPointerException if either timeout or retryCondition are null.
     * @throws IllegalArgumentException if maxAttempts is less than -1.
     */
    public ConstantRetryer(final Duration timeout, final int maxAttempts,
                           final RetryCondition<T> retryCondition) {
        super(maxAttempts, retryCondition);
        this.timeout = Objects.requireNonNull(timeout);
    }

    /**
     * Create a {@link ConstantRetryer} with a {@link DefaultRetryCondition}.
     *
     * @param timeout the amount of time to wait between retries.
     * @param maxAttempts maximum number of attempts to perform. If value is -1 it will retry indefinitely.
     *
     * @throws NullPointerException if either timeout or retryCondition are null.
     * @throws IllegalArgumentException if maxAttempts is less than -1.
     */
    public ConstantRetryer(final Duration timeout, final int maxAttempts) {
        this(timeout, maxAttempts, new DefaultRetryCondition<>());
    }

    /**
     * Create a {@link ConstantRetryer} with a {@link DefaultRetryCondition}
     * and a maximum number of retries equals to DEFAULT_MAX_ATTEMPTS.
     *
     * @param timeout the amount of time to wait between retries.
     *
     * @throws NullPointerException if timeout is null.
     */
    public ConstantRetryer(final Duration timeout) {
        this(timeout, DEFAULT_MAX_ATTEMPTS, new DefaultRetryCondition<>());
    }

    /**
     * Create a {@link ConstantRetryer} with a {@link DefaultRetryCondition}
     * and a maximum number of retries equals to DEFAULT_MAX_ATTEMPTS.
     *
     * <p>The constant amount of time to wait between retries is zero.
     */
    public ConstantRetryer() {
        this(Duration.ZERO, DEFAULT_MAX_ATTEMPTS, new DefaultRetryCondition<>());
    }

    /**
     * Waits a constant amount of time between the retries.
     *
     * @param attempt the number of attempt done so far. This value is not used here.
     */
    @Override
    protected final void wait(final int attempt) {
        try {
            TimeUnit.SECONDS.sleep(timeout.getSeconds());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @return the timeout to wait between retries.
     */
    public final Duration getTimeout() {
        return timeout;
    }
}
