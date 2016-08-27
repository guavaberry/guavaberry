package org.guavaberry.retryer;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.WaitStrategy;
import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;
import java.time.Duration;
import java.util.Objects;
import java.util.Random;

/**
 * Factory class for instances of {@link WaitStrategy}.
 *
 * <p>These factories return wait strategies that can be used with the library
 * <a href=https://github.com/rholder/guava-retrying>Guava Retrying</a>.
 *
 * <p>There are three wait strategies documented below: {@link ExponentialJitterWaitStrategy},
 * {@link ExponentialWaitStrategy} and {@link CompositeJitterWaitStrategy}.
 *
 * <h2>{@link ExponentialJitterWaitStrategy}</h2>
 * This implementation of {@link WaitStrategy} is helpful whenever there are competing clients that
 * are trying to call a remote server at the same time.
 *
 * <p>The main benefits of this strategy is that it avoids overwhelming the server by multiple requests
 * by slowing clients down using the classic exponential backoff.
 * Moreover, since the reason of an impaired server might be due to the wave of requests coming
 * from the clients, this strategy introduce an element of randomness (aka jitter) that ensures
 * the requests are spread out along a randomized interval that grows exponentially for each attempt.
 * The retry is capped to a maximum timeout in order to avoid the interval to grow indefinitely.
 *
 * <p>The interval for the i-th retry is calculated in the following way:
 * {@code
 *   min_value = exponential_wait * (1 - randomization_factor)
 *   max_value = exponential_wait
 * }
 *
 * <p>Where {@code exponential_wait} is the exponential time calculated for the i-th attempt and
 * {@code randomization_factor} is a double belonging to the interval [0.0, 1.0].
 *
 * <p>Example:
 * Let's suppose that the randomization factor is 0.5, the base timeout is one second and the max timeout is 10 seconds.
 * For five retries the sequence will be (values in seconds):
 *
 * <table summary="">
 *   <tr>
 *      <td>Attempt</td><td>Interval</td>
 *   </tr>
 *   <tr>
 *      <td>1</td><td>[0.5, 1]</td>
 *   </tr>
 *   <tr>
 *      <td>2</td><td>[1, 2]</td>
 *   </tr>
 *   <tr>
 *      <td>3</td><td>[2, 4]</td>
 *   </tr>
 *   <tr>
 *      <td>4</td><td>[4, 8]</td>
 *   </tr>
 *   <tr>
 *      <td>5</td><td>[5, 10]</td>
 *   </tr>
 * </table>
 *
 * <p>Choose the randomization factor wisely according to your use case.
 * The more the randomization factor tends to zero the more the retry behavior will look like the classic
 * exponential backoff. This can be useful whenever we know that the cause of an impaired server is not due to the
 * competing clients. Conversely, the more the randomization factor tends to one the more the requests will be
 * spread along the interval, allowing the server to digest the request more gradually and reducing contention
 * between competing clients.
 *
 * <h2>{@link ExponentialWaitStrategy}</h2>
 * This is a refactor of {@link com.github.rholder.retry.WaitStrategies#exponentialWait()} class based on
 * {@link Duration} instead of long for declaring the delays.
 *
 * <p>The main benefits of this strategy is that it avoids overwhelming the server by multiple requests
 * by slowing clients down using the classic exponential backoff.
 * The retry is capped to a maximum timeout in order to avoid the interval to grow indefinitely.
 *
 * <p>The timeout for the i-th attempt is calculated in the following way:
 * {@code
 *   exponential_wait = min(max_timeout, 2**(attempts-1))
 * }
 *
 * <p>Where {@code attempt} is the number of current attempts and {@code max_timeout} is the capped value for the
 * calculated timeout.
 *
 * <p>Example:
 * Let's suppose that the base timeout is one second and the max timeout is 10 seconds.
 * For five retries the sequence will be (values in seconds):
 *
 * <table summary="">
 *   <tr>
 *      <td>Attempt</td><td>Timeout</td>
 *   </tr>
 *   <tr>
 *      <td>1</td><td>1</td>
 *   </tr>
 *   <tr>
 *      <td>2</td><td>2</td>
 *   </tr>
 *   <tr>
 *      <td>3</td><td>4</td>
 *   </tr>
 *   <tr>
 *      <td>4</td><td>8</td>
 *   </tr>
 *   <tr>
 *      <td>5</td><td>10</td>
 *   </tr>
 * </table>
 *
 * <p>This strategy is equivalent to {@link ExponentialJitterWaitStrategy} with randomization factor equals to zero.
 *
 * <h2>CompositeJitterWaitStrategy</h2>
 * This implementation of {@link WaitStrategy} helps you combining an existing waiting strategy with
 * a randomization factor.
 *
 * <p>The interval for the i-th retry is calculated in the following way:
 * {@code
 *   min_value = wait * (1 - randomization_factor)
 *   max_value = wait
 * }
 *
 * <p>Where {@code wait} is the wait time calculated for the i-th attempt by the existing waiting strategy and
 * {@code randomization_factor} is a double belonging to the interval [0.0, 1.0].
 *
 *
 * @author Filippo Squillace
 *
 * @see com.github.rholder.retry.Retryer
 * @see com.github.rholder.retry.WaitStrategies
 */
public final class WaitStrategies {

    /**
     * Private constructor.
     */
    private WaitStrategies() { }

    /**
     * The default randomization factor (0.5).
     */
    private static final double DEFAULT_RANDOMIZATION_FACTOR = 0.5D;

    /**
     * The default base timeout (1 second).
     */
    private static final Duration DEFAULT_BASE_TIMEOUT = Duration.ofSeconds(1L);

    /**
     * Return an instance of {@link ExponentialJitterWaitStrategy} with the default base timeout (1 second), the default
     * randomization factor (0.5) and without a max timeout.
     *
     * @return instance of {@link ExponentialJitterWaitStrategy}
     */
    public static WaitStrategy exponentialJitterWait() {
        return exponentialJitterWait(DEFAULT_BASE_TIMEOUT,
                                     Duration.ofMillis(Long.MAX_VALUE), DEFAULT_RANDOMIZATION_FACTOR);
    }

    /**
     * Return an instance of {@link ExponentialJitterWaitStrategy} with the default base timeout (1 second) and
     * the default randomization factor (0.5).
     *
     * @param maxTimeout the calculated time will be capped to this value.
     *
     * @return instance of {@link ExponentialJitterWaitStrategy}
     */
    public static WaitStrategy exponentialJitterWait(final Duration maxTimeout) {
        return exponentialJitterWait(DEFAULT_BASE_TIMEOUT, maxTimeout, DEFAULT_RANDOMIZATION_FACTOR);
    }

    /**
     * Return an instance of {@link ExponentialJitterWaitStrategy} with the default randomization factor (0.5).
     *
     * @param baseTimeout the base time delay.
     * @param maxTimeout the calculated time will be capped to this value.
     *
     * @return instance of {@link ExponentialJitterWaitStrategy}
     */
    public static WaitStrategy exponentialJitterWait(final Duration baseTimeout, final Duration maxTimeout) {
        return exponentialJitterWait(baseTimeout, maxTimeout, DEFAULT_RANDOMIZATION_FACTOR);
    }

    /**
     * Return an instance of {@link ExponentialJitterWaitStrategy} with the default base timeout (1 second).
     *
     * @param maxTimeout the calculated time will be capped to this value.
     * @param randomizationFactor the randomization factor for creating a range around the retry interval.
     *
     * @return instance of {@link ExponentialJitterWaitStrategy}
     */
    public static WaitStrategy exponentialJitterWait(final Duration maxTimeout,
                                                     final double randomizationFactor) {
        return exponentialJitterWait(DEFAULT_BASE_TIMEOUT, maxTimeout, randomizationFactor);
    }

    /**
     * Return an instance of {@link ExponentialJitterWaitStrategy}.
     *
     * @param baseTimeout the base time delay.
     * @param maxTimeout the calculated time will be capped to this value.
     * @param randomizationFactor the randomization factor for creating a range around the retry interval.
     *
     * @return instance of {@link ExponentialJitterWaitStrategy}
     */
    public static WaitStrategy exponentialJitterWait(final Duration baseTimeout,
                                                     final Duration maxTimeout,
                                                     final double randomizationFactor) {
        return new ExponentialJitterWaitStrategy(baseTimeout, maxTimeout,
                                                 randomizationFactor);
    }

    /**
     * Return an instance of {@link ExponentialWaitStrategy}.
     *
     * @param baseTimeout the base time delay.
     * @param maxTimeout the calculated time will be capped to this value.
     *
     * @return instance of {@link ExponentialWaitStrategy}
     */
    public static WaitStrategy exponentialWait(final Duration baseTimeout,
                                               final Duration maxTimeout) {
        return new ExponentialWaitStrategy(baseTimeout, maxTimeout);
    }

    /**
     * Return an instance of {@link ExponentialWaitStrategy}.
     *
     * @param maxTimeout the calculated time will be capped to this value.
     *
     * @return instance of {@link ExponentialWaitStrategy}
     */
    public static WaitStrategy exponentialWait(final Duration maxTimeout) {
        return new ExponentialWaitStrategy(DEFAULT_BASE_TIMEOUT, maxTimeout);
    }

    /**
     * Return an instance of {@link CompositeJitterWaitStrategy}.
     *
     * @param waitStrategy the wait strategy used for combining with the random interval
     *                     generated by the randomization factor.
     * @param randomizationFactor the randomization factor for creating a range around the retry interval.
     *
     * @return instance of {@link CompositeJitterWaitStrategy}
     */
    public static WaitStrategy compositeJitterWait(final WaitStrategy waitStrategy,
                                                   final double randomizationFactor) {
        return new CompositeJitterWaitStrategy(waitStrategy, randomizationFactor);
    }

    /**
     * The wait strategy that combines the exponential behavior with randomness based on a randomization factor.
     */
    @Immutable
    private static final class ExponentialJitterWaitStrategy implements WaitStrategy {
        /**
         * The wait strategy used for calculate the random range around the exponential retry interval.
         */
        private final WaitStrategy jitterWaitStrategy;

        /**
         * Construct an instance of {@link ExponentialJitterWaitStrategy}.
         *
         * @param baseTimeout the base time delay.
         * @param maxTimeout the calculated time will be capped to this value.
         * @param randomizationFactor the randomization factor for creating a range around the retry interval.
         */
        ExponentialJitterWaitStrategy(final Duration baseTimeout, final Duration maxTimeout,
                                             final double randomizationFactor) {
            jitterWaitStrategy = new CompositeJitterWaitStrategy(exponentialWait(baseTimeout, maxTimeout),
                                                                 randomizationFactor);
        }

        /**
         * @param failedAttempt the current failed attempt.
         * @return the calculated wait time in milliseconds.
         */
        public long computeSleepTime(final Attempt failedAttempt) {
            Objects.requireNonNull(failedAttempt, "The failedAttempt may not be null");
            return jitterWaitStrategy.computeSleepTime(failedAttempt);
        }
    }

    /**
     * The wait strategy for exponential back off.
     */
    @Immutable
    private static final class ExponentialWaitStrategy implements WaitStrategy {
        /**
         * The base time delay.
         */
        private final Duration baseTimeout;

        /**
         * The calculated time will be capped to this value.
         */
        private final Duration maxTimeout;

        /**
         * Construct an instance of {@link ExponentialWaitStrategy}.
         *
         * @param baseTimeout the base time delay.
         * @param maxTimeout the calculated time will be capped to this value.
         */
        ExponentialWaitStrategy(final Duration baseTimeout,
                                       final Duration maxTimeout) {
            Objects.requireNonNull(baseTimeout, "The base timeout may not be null");
            Objects.requireNonNull(maxTimeout, "The maximum timeout may not be null");
            Preconditions.checkArgument(baseTimeout.toMillis() >= 0L, "baseTimeout must be > 0 but is %s", baseTimeout);
            Preconditions.checkArgument(maxTimeout.toMillis() >= 0L, "maxTimeout must be >= 0 but is %s", maxTimeout);
            this.baseTimeout = baseTimeout;
            this.maxTimeout = maxTimeout;
        }

        /**
         * @param failedAttempt the current failed attempt.
         * @return the calculated wait time in milliseconds.
         */
        @Override
        public long computeSleepTime(final Attempt failedAttempt) {
            Objects.requireNonNull(failedAttempt, "The failedAttempt may not be null");
            double exp = Math.pow(2, failedAttempt.getAttemptNumber() - 1);
            long waitTime = Math.round(baseTimeout.toMillis() * exp);

            if (waitTime > maxTimeout.toMillis()) {
                return maxTimeout.toMillis();
            }
            return waitTime;
        }
    }

    /**
     * This implementation of {@link WaitStrategy} helps you combining an existing waiting strategy with
     * a randomization factor.
     */
    @Immutable
    private static final class CompositeJitterWaitStrategy implements WaitStrategy {
        /**
         * The wait strategy used for combining with the random interval generated by the randomization factor.
         */
        private final WaitStrategy waitStrategy;
        /**
         * The randomization factor for creating a range around the retry interval.
         */
        private final double randomizationFactor;

        /**
         * Construct an instance of {@link CompositeJitterWaitStrategy}.
         *
         * @param waitStrategy the wait strategy used for combining with the random interval
         *                     generated by the randomization factor.
         * @param randomizationFactor the randomization factor for creating a range around the retry interval.
         */
        CompositeJitterWaitStrategy(final WaitStrategy waitStrategy, final double randomizationFactor) {
            Objects.requireNonNull(waitStrategy, "The wait strategy may not be null");
            Preconditions.checkArgument(randomizationFactor >= 0.0D, "randomizationFactor must be >= 0.0 but is %s",
                                        randomizationFactor);
            Preconditions.checkArgument(randomizationFactor <= 1.0D, "randomizationFactor must be >= 1.0 but is %s",
                                        randomizationFactor);
            this.waitStrategy = waitStrategy;
            this.randomizationFactor = randomizationFactor;
        }

        /**
         * @param failedAttempt the current failed attempt.
         * @return the calculated wait time in milliseconds.
         */
        public long computeSleepTime(final Attempt failedAttempt) {
            Objects.requireNonNull(failedAttempt, "The failedAttempt may not be null");
            long waitTime = waitStrategy.computeSleepTime(failedAttempt);
            long minRange = (long) (waitTime * (1 - randomizationFactor));

            return new Random().longs(1L, minRange, waitTime + 1).findFirst().getAsLong();
        }
    }
}
