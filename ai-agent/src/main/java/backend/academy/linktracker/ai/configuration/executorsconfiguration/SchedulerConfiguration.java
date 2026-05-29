package backend.academy.linktracker.ai.configuration.executorsconfiguration;

import backend.academy.linktracker.ai.properties.GroupingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfiguration {

    @Bean(name = "linkUpdateGrouperScheduler")
    public TaskScheduler getAuctionScheduler(GroupingProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getCorePoolSize());
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.setThreadNamePrefix("auction-");
        scheduler.initialize();
        return scheduler;
    }
}
