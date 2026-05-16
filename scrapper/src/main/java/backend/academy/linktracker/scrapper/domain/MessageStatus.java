package backend.academy.linktracker.scrapper.domain;

public enum OutboxEventStatus {
    PENDING,
    SENT,
    FAILED,
}
