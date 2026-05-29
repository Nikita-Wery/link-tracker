package backend.academy.linktracker.ai.model;

import java.util.Queue;

public record LinkUpdatesGroup(long key, Queue<ProcessedLinkUpdate> processedLinkUpdates) {}
