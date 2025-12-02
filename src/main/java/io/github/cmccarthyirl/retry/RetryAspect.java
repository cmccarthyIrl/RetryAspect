package io.github.cmccarthyirl.retry;

import io.github.cmccarthyirl.annotations.Retryable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Aspect for handling retry logic on methods annotated with {@link Retryable}.
 */
@Aspect
public class RetryAspect {

    /**
     * Pointcut that matches methods annotated with {@link Retryable}.
     *
     * @param retryable the {@link Retryable} annotation instance
     */
    @Pointcut("@annotation(retryable) && execution(* *(..))")
    public void retryableMethods(Retryable retryable) {
    }

    /**
     * Around advice that retries the method execution based on the {@link Retryable} annotation.
     * <p>
     * Note: maxAttempts represents the total number of attempts (initial attempt + retries).
     * For example, maxAttempts=3 means 1 initial attempt + up to 2 retries.
     *
     * @param joinPoint the join point representing the method execution
     * @param retryable the {@link Retryable} annotation instance
     * @return the result of the method execution
     * @throws Throwable if the method execution fails after the maximum retry attempts
     */
    @Around(value = "retryableMethods(retryable)", argNames = "joinPoint,retryable")
    public Object retryMethod(ProceedingJoinPoint joinPoint, Retryable retryable) throws Throwable {
        int maxAttempts = retryable.maxAttempts(); // Maximum number of retry attempts
        long delay = retryable.backoff().delay(); // Delay between retries in milliseconds
        double multiplier = retryable.backoff().multiplier(); // Multiplier for backoff
        Class<? extends Throwable>[] retryExceptions = retryable.include(); // Exceptions to retry on

        // Validate configuration
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, but was: " + maxAttempts);
        }
        if (retryExceptions.length == 0) {
            throw new IllegalArgumentException("include array cannot be empty. Specify at least one exception type to retry on.");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("delay must be non-negative, but was: " + delay);
        }

        Throwable lastException = null; // Store the last exception encountered
        int attempts = 0;
        // Retry logic loop
        while (attempts < maxAttempts) {
            try {
                return joinPoint.proceed();
            } catch (Throwable ex) {
                lastException = ex;
                if (!isExceptionInIncludedList(ex, retryExceptions)) {
                    throw ex;
                }
                attempts++;
                if (attempts < maxAttempts) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // Restore interrupt status
                        throw new RuntimeException("Retry interrupted after " + attempts + " attempt(s)", ie);
                    }
                    delay = (long) (delay * multiplier); // Increase delay according to multiplier
                }
            }
        }
        throw lastException;
    }

    /**
     * Helper method to check if an exception is in the included classes for retry.
     *
     * @param ex              the exception to check
     * @param includedClasses the array of exception classes to include
     * @return true if the exception is in the included classes, false otherwise
     */
    private boolean isExceptionInIncludedList(Throwable ex, Class<? extends Throwable>[] includedClasses) {
        for (Class<? extends Throwable> includedClass : includedClasses) {
            if (includedClass.isInstance(ex)) {
                return true;
            }
        }
        return false;
    }
}
