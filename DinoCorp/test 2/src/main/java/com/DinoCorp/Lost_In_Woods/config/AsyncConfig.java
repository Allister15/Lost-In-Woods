package com.DinoCorp.Lost_In_Woods.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Enables @Async and backs the Rolling Async Background Pipeline with a
// virtual-thread-per-task executor (Java 21). Story generation is I/O-bound (it blocks
// on the LLM HTTP call), which is exactly what virtual threads excel at: each prefetch
// task gets its own lightweight thread, so a slow model parks a virtual thread instead
// of pinning a scarce platform thread — the carrier pool stays free for live turns.
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String STORY_GEN_EXECUTOR = "storyGenExecutor";

    @Bean(STORY_GEN_EXECUTOR)
    public Executor storyGenExecutor() {
        // One fresh virtual thread per generation task; unbounded but cheap.
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
