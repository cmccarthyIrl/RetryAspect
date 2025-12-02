package io.github.cmccarthyirl.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to configure the backoff strategy for retry operations.
 * <p>
 * This annotation allows configuring the delay and multiplier for
 * exponential backoff when retrying operations.
 * <p>
 * Example usage:
 * <p>
 * {@code @Backoff(delay = 1000, multiplier = 1.5)}
 * <p>
 * Attributes:
 * - `delay`: Initial delay before retrying the operation, in milliseconds.
 * - `multiplier`: Factor by which the delay is multiplied after each retry.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Backoff {
    /**
     * The initial delay before retrying the operation, in milliseconds.
     * Must be non-negative. Default is 1000ms (1 second).
     *
     * @return the initial delay
     */
    int delay() default 1000;

    /**
     * The multiplier to apply to the delay for each subsequent retry.
     *
     * @return the multiplier
     */
    double multiplier() default 1.0;
}
