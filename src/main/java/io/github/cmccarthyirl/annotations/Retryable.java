package io.github.cmccarthyirl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method or type is retryable.
 * <p>
 * This annotation specifies the maximum number of retry attempts and the
 * backoff strategy to use in case of failure. It also allows specifying
 * which exceptions should trigger a retry.
 * <p>
 * <strong>Important:</strong> Methods annotated with @Retryable should be idempotent,
 * as they may be executed multiple times. Non-idempotent operations (e.g., payments,
 * inserts) may cause unintended side effects if retried.
 * <p>
 * Example usage:
 * <p>
 * {@code @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2.0), include = {IOException.class, SQLException.class})}
 * <p>
 * Attributes:
 * - `maxAttempts`: Total number of attempts (initial + retries). Must be at least 1.
 * - `backoff`: Backoff strategy to use between retries.
 * - `include`: Array of exception classes that should trigger a retry. Cannot be empty.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Retryable {
    /**
     * The maximum number of total attempts (initial attempt + retries).
     * Must be at least 1. Default is 3 (1 initial + 2 retries).
     *
     * @return the maximum number of attempts
     */
    int maxAttempts() default 3;

    /**
     * The backoff strategy to use between retry attempts.
     *
     * @return the backoff strategy
     */
    Backoff backoff() default @Backoff;

    /**
     * The exceptions that should trigger a retry.
     * This array cannot be empty - at least one exception type must be specified.
     *
     * @return array of exception classes to include
     */
    Class<? extends Throwable>[] include();
}