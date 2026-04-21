package backend.academy.linktracker.scrapper.config.executorsconfiguration;

import backend.academy.linktracker.scrapper.properties.LinkUpdateWorkerProperties;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ExecutorsConfiguration {

    @Bean(name = "linkUpdateWorkerExecutor")
    public ThreadPoolTaskExecutor linkUpdateWorkerExecutor(LinkUpdateWorkerProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix("db-linkUpdate-worker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();

        return executor;
    }

    @Bean(name = "externalApiExecutor")
    public ThreadPoolTaskExecutor externalApiExecutor(SchedulerProperties schedulerProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(schedulerProperties.getThreadPoolSize());
        executor.setMaxPoolSize(schedulerProperties.getThreadPoolSize());
        executor.setQueueCapacity(schedulerProperties.getQueueCapacity());

        executor.setThreadNamePrefix("link-worker-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        return executor;
    }

}
