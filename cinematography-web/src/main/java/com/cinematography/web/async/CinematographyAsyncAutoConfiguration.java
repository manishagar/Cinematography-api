package com.cinematography.web.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.jspecify.annotations.NonNull;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Enables {@code @Async} with a context-propagating executor named {@value #EXECUTOR}.
 * <p>
 * Opt-in with {@code cinematography.async.enabled=true}: services that never enabled async (admin-api, token-api) keep
 * running their {@code @Async} methods synchronously.
 */
@AutoConfiguration
@EnableAsync
@ConditionalOnProperty(prefix = "cinematography.async", name = "enabled", havingValue = "true")
@Slf4j
public class CinematographyAsyncAutoConfiguration implements AsyncConfigurer {

    /** Bean name to use in {@code @Async(CinematographyAsyncAutoConfiguration.EXECUTOR)}. */
    public static final String EXECUTOR = "cinematographyAsyncExecutor";

    @Value("${cinematography.async.core-pool-size:10}")
    private int corePoolSize;

    @Value("${cinematography.async.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${cinematography.async.queue-capacity:100}")
    private int queueCapacity;

    @Override
    @Bean(name = EXECUTOR)
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor baseExecutor = new ThreadPoolTaskExecutor();
        baseExecutor.setCorePoolSize(corePoolSize);
        baseExecutor.setMaxPoolSize(maxPoolSize);
        baseExecutor.setQueueCapacity(queueCapacity);
        baseExecutor.setThreadNamePrefix("async-exec-");
        baseExecutor.initialize();

        return new ContextPropagatingExecutor(baseExecutor);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
        log.error("Async error in method '{}': {}", method.getName(), ex.getMessage(), ex);
    }

    /**
     * Wrapper executor that captures and restores Micrometer context (e.g., trace/span ID)
     */
    public static class ContextPropagatingExecutor implements AsyncTaskExecutor {

        private final AsyncTaskExecutor delegate;

        public ContextPropagatingExecutor(AsyncTaskExecutor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(Runnable task) {
            delegate.execute(snapshot().wrap(task));
        }

        @Override
        public Future<?> submit(@NonNull Runnable task) {
            return delegate.submit(snapshot().wrap(task));
        }

        @Override
        public <T> Future<T> submit(@NonNull Callable<T> task) {
            return delegate.submit(snapshot().wrap(task));
        }

        private static ContextSnapshot snapshot() {
            return ContextSnapshotFactory.builder()
                    .contextRegistry(ContextRegistry.getInstance())
                    .build()
                    .captureAll();
        }

        // Do NOT override the deprecated method
    }
}
