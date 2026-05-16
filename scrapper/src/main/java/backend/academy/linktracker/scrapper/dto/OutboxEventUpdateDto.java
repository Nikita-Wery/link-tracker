package backend.academy.linktracker.scrapper.dto;

import backend.academy.linktracker.scrapper.domain.MessageStatus;

public record OutboxEventUpdateDto(Long outboxEventId, MessageStatus futureMessageStatuses) {}
