package org.guavaberry.retryer;

import com.google.common.annotations.VisibleForTesting;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link Retryer} that times out a linear amount of time between retries.
 *
 * @param <T> the type of the return value.
 */
public class LinearRetryer<T> extends BaseRetryer<T> {
    /**
     * The base of time used in linear back-off strategy.
     */
    private final Duration baseTimeout;

    /**
     * The calculated time will be capped to this value.
     */
    private final Duration maxTimeout;

    /**
     * Creates a {@link LinearRetryer} instance that retries until a
     * maximum attempts maxAttempts. It accepts a {@link RetryCondition} for deciding
     * whether the retry should occur depending on either the returned value or
     * the exception raised by the lambda function.
     * If maxAttempts is -1 the instance will attempt to retry indefinitely until {@link RetryCondition} has
     * satisfied.
     *
     * <p>This implementation waits a linear amount of time defined by baseTimeout.
     *
     * @param baseTimeout the base tim used in linear back-off strategy.
     * @param maxTimeout the calculated time will be capped to this value.
     * @param maxAttempts maximum number of attempts to perform. If value is -1 it will retry indefinitely.
     * @param retryCondition contains the logic for deciding whether a retry should occur or not.
     *
     * @throws NullPointerException if either baseTimeout, maxTimeout or retryCondition are null.
     * @throws IllegalArgumentException if maxAttempts is less than -1.
     */
    public LinearRetryer(final Duration baseTimeout, final Duration maxTimeout, final int maxAttempts,
                         final RetryCondition<T> retryCondition) {
        super(maxAttempts, retryCondition);
        this.baseTimeout = Objects.requireNonNull(baseTimeout);
        this.maxTimeout = Objects.requireNonNull(maxTimeout);
    }

    /**
     * Create a {@link LinearRetryer} with a {@link DefaultRetryCondition}.
     *
     * @param baseTimeout the amount of time to wait between retries.
     * @param maxTimeout the calculated time will be capped to this value.
     * @param maxAttempts maximum number of attempts to perform. If value is -1 it will retry indefinitely.
     *
     * @throws NullPointerException if either baseTimeout or maxTimeout are null.
     * @throws IllegalArgumentException if maxAttempts is less than -1.
     */
    public LinearRetryer(final Duration baseTimeout, final Duration maxTimeout, final int maxAttempts) {
        this(baseTimeout, maxTimeout, maxAttempts, new DefaultRetryCondition<>());
    }

    /**
     * Create a {@link LinearRetryer} with a {@link DefaultRetryCondition}
     * and a maximum number of retries equals to DEFAULT_MAX_ATTEMPTS.
     *
     * @param baseTimeout the amount of time to wait between retries.
     * @param maxTimeout the calculated time will be capped to this value.
     *
     * @throws NullPointerException if either baseTimeout or maxTimeout are null.
     */
    public LinearRetryer(final Duration baseTimeout, final Duration maxTimeout) {
        this(baseTimeout, maxTimeout, DEFAULT_MAX_ATTEMPTS, new DefaultRetryCondition<>());
    }

    /**
     * Create a {@link LinearRetryer} with a {@link DefaultRetryCondition}
     * and a maximum number of retries equals to DEFAULT_MAX_ATTEMPTS.
     *
     * <p>The constant amount of time to wait between retries is zero.
     * An instance of {@link LinearRetryer} created with default constructor is equivalent
     * to a {@link ConstantRetryer} instance with zero timeout.
     *
     * @see ConstantRetryer
     */
    public LinearRetryer() {
        this(Duration.ZERO, Duration.ZERO, DEFAULT_MAX_ATTEMPTS, new DefaultRetryCondition<>());
    }

    /**
     * Waits a constant amount of time between the retries.
     *
     * <p>The actual timeout before attempting the next retry is calculates as follow:
     * {@code
     *   timeout = min(maxTimeout, baseTimeout*attempt)
     * }
     *
     * @param attempt the number of attempt done so far.
     */
    @Override
    protected final void wait(final int attempt) {
        try {
            long cappedTimeout = calculateTimeout(attempt).getSeconds();
            TimeUnit.SECONDS.sleep(cappedTimeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Calculate the timeout with a linear function on the number of attempts.
     *
     * <p>The actual timeout before attempting the next retry is calculates as follow:
     * {@code
     *   timeout = min(maxTimeout, baseTimeout*attempt)
     * }
     *
     * @param attempt the number of attempt done so far.
     * @return the calculated timeout.
     */
    @VisibleForTesting
    final Duration calculateTimeout(final int attempt) {
        Duration calculatedTimeout = baseTimeout.multipliedBy(attempt);
        return (calculatedTimeout.compareTo(maxTimeout) < 0) ? calculatedTimeout : maxTimeout;
    }

    /**
     * @return the base timeout to wait between retries.
     */
    public final Duration getBaseTimeout() {
        return baseTimeout;
    }

    /**
     * @return the max timeout to wait between retries.
     */
    public final Duration getMaxTimeout() {
        return maxTimeout;
    }
}
