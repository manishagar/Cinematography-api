package com.cinematography.web.observability;

import java.util.Locale;
import java.util.concurrent.Callable;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Cross-cutting observability helper for timing and error logging using Micrometer Observation.
 * <p>
 * Log lines are tagged with the service label (the upper-cased {@code spring.application.name}, e.g.
 * {@code HS-API}). By default a failure returns null; use {@code observeAndRethrow} for fail-fast semantics.
 */
@Slf4j
public class ProcedureObserver {

    private final ObservationRegistry observationRegistry;
    private final String durationLog;
    private final String failedLog;

    public ProcedureObserver(ObservationRegistry observationRegistry, String applicationName) {
        this.observationRegistry = observationRegistry;
        String label = applicationName.toUpperCase(Locale.ROOT);
        this.durationLog = ">>> [" + label + ".{}.{}] | Duration={} ms <<<";
        this.failedLog = ">>> [" + label + ".{}.{}] -> failed: {} <<<";
    }

    /**
     * Observes execution time and logs errors. Returns null on exception.
     * @param procedureName metric/log key (e.g., "lcapi.sp.web_ci_api_landedCostUnits")
     * @param callable the code to execute
     * @param caller used for contextual logging of the caller's simple class name
     */
    public <T> T observeProcedure(String procedureName, Callable<T> callable, Object caller) {
        final String className = (caller != null ? caller.getClass().getSimpleName() : "UnknownClass");

        return Observation.createNotStarted(procedureName, observationRegistry).observe(() -> {
            final long startTime = System.currentTimeMillis();
            try {
                return callable.call();
            } catch (Exception e) {
                log.error(failedLog, className, procedureName, e.getMessage(), e);
                return null; // keep current contract
            } finally {
                final long duration = System.currentTimeMillis() - startTime;
                log.info(durationLog, className, procedureName, duration);
            }
        });
    }

    /** Convenience overload that uses this bean as 'caller'. */
    public <T> T observeProcedure(String procedureName, Callable<T> callable) {
        return observeProcedure(procedureName, callable, this);
    }

    /**
     * Observes and rethrows exceptions (wrapped into IllegalStateException if checked).
     * Prefer this for fail-fast semantics; eliminates null handling at call sites.
     */
    public <T> T observeAndRethrow(String procedureName, Callable<T> callable, Object caller) {
        final String className = (caller != null ? caller.getClass().getSimpleName() : "UnknownClass");

        return Observation.createNotStarted(procedureName, observationRegistry).observe(() -> {
            final long startTime = System.currentTimeMillis();
            try {
                return callable.call();
            } catch (RuntimeException re) {
                log.error(failedLog, className, procedureName, re.getMessage(), re);
                throw re;
            } catch (Exception e) {
                log.error(failedLog, className, procedureName, e.getMessage(), e);
                throw new IllegalStateException("Observed call failed: " + procedureName, e);
            } finally {
                final long duration = System.currentTimeMillis() - startTime;
                log.info(durationLog, className, procedureName, duration);
            }
        });
    }

    public <T> T observeAndRethrow(String procedureName, Callable<T> callable) {
        return observeAndRethrow(procedureName, callable, this);
    }
}
