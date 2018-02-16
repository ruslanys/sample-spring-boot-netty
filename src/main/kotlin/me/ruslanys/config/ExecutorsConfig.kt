package me.ruslanys.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.util.concurrent.Executor


@Configuration
@EnableScheduling
@EnableAsync
class ExecutorsConfig : SchedulingConfigurer, AsyncConfigurer {

    companion object {
        private const val SCHEDULER_POOL_SIZE = 5
        private const val ASYNC_POOL_SIZE = 10
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = SCHEDULER_POOL_SIZE
        taskScheduler.initialize()
        taskScheduler.threadNamePrefix = "ScheduledExecutor-"

        taskRegistrar.setTaskScheduler(taskScheduler)
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler =
            AsyncUncaughtExceptionHandler { throwable, _, _ ->
                LoggerFactory.getLogger("Async").error("Async error", throwable)
            }

    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = ASYNC_POOL_SIZE
        executor.threadNamePrefix = "AsyncExecutor-"
        executor.initialize()
        return executor
    }


}