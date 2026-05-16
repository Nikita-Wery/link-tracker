package backend.academy.linktracker.scrapper.properties;

public interface WorkerProperties {

    int getMaximumNumberOfUnprocessedElements();

    int getBatchSize();

    long getFlushTimeout();

    int getCorePoolSize();

    int getMaxPoolSize();

    int getQueueCapacity();
}
