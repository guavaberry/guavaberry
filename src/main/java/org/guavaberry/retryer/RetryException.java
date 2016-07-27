package org.guavaberry.retryer;

import javax.annotation.Nullable;

/**
 * Exception used whenever the number of retries exceeded the maximum value.
 */
public class RetryException extends RuntimeException {
    /**
     * The number of attempts performed before the exception has been raised.
     */
    private final int attempts;
    /**
     * The last returned value before the exception has been raised.
     */
    @Nullable private final Object lastReturnValue;
    /**
     * The last {@link Throwable} before the exception has been raised.
     */
    @Nullable private final Throwable lastThrowable;

    /**
     * Creates a new instance of {@link RetryException}.
     *
     * @param message the message to show when exception is raised.
     * @param attempts the number of attempts performed before the exception has been raised.
     * @param lastReturnValue the last returned value before the exception has been raised.
     * @param lastThrowable the last {@link Throwable} before the exception has been raised.
     */
    public RetryException(final String message, final int attempts,
                          @Nullable final Object lastReturnValue, @Nullable final Throwable lastThrowable) {
        super(message, lastThrowable);
        this.attempts = attempts;
        this.lastReturnValue = lastReturnValue;
        this.lastThrowable = lastThrowable;
    }

    /**
     * @return the number of attempts.
     */
    public final int getAttempts() {
        return attempts;
    }

    /**
     * @return the last return value.
     */
    public final Object getLastReturnValue() {
        return lastReturnValue;
    }

    /**
     * @return the last throwable.
     */
    public final Throwable getLastThrowable() {
        return lastThrowable;
    }

    /**
     * @return the message to show when the exception is raised. It is composed
     *         by all the properties of {@link RetryException} instance.
     */
    @Override
    public final String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getMessage());
        sb.append(": maxAttempts=" + getAttempts());
        sb.append(", lastReturnValue=" + getLastReturnValue());
        sb.append(", lastException=" + getLastThrowable());
        return sb.toString();
    }

}
