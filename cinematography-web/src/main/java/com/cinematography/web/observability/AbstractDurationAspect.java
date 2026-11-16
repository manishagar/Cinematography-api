package com.cinematography.web.observability;

import java.util.Locale;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Logs how long a join point took, tagged with the service label (upper-cased {@code spring.application.name}).
 * <p>
 * Each service keeps a small {@code @Aspect @Component} subclass that declares only its own pointcut:
 * <pre>
 * &#64;Around("execution(public * com.dsg.hsapi.controller..*(..))")
 * public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
 *     return timed(joinPoint);
 * }
 * </pre>
 * Log lines are written under the subclass's logger, as before.
 */
public abstract class AbstractDurationAspect {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String durationLog;

    @Value("${spring.application.name}")
    void setApplicationName(String applicationName) {
        this.durationLog = ">>> [" + applicationName.toUpperCase(Locale.ROOT) + ".{}.{}] | Duration={} ms <<<";
    }

    protected Object timed(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.info(durationLog, className, methodName, duration);

        return result;
    }
}
