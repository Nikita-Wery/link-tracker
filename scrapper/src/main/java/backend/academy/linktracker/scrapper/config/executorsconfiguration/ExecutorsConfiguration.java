package backend.academy.linktracker.scrapper.config.executorsconfiguration;

import backend.academy.linktracker.scrapper.properties.ApiWorkersProperties;
import backend.academy.linktracker.scrapper.properties.LinkUpdateWorkerProperties;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
    public ThreadPoolTaskExecutor externalApiExecutor(ApiWorkersProperties apiWorkersProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(apiWorkersProperties.getThreadPoolSize());
        executor.setMaxPoolSize(apiWorkersProperties.getThreadPoolSize());
        executor.setQueueCapacity(apiWorkersProperties.getQueueCapacity());

        executor.setThreadNamePrefix("link-api-worker-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        return executor;
    }
}
