package org.guavaberry.function;

/**
 * Function that accepts no argument and returns an instance of {@link T}.
 * Useful for factory instance creation.
 *
 * <p>https://en.wikipedia.org/wiki/Arity
 *
 * @param <T> type of the instance to return
 *
 */
public interface NullaryOperator<T> {

    /**
     *
     * @return an instance of {@link T}
     */
    T op();
}
