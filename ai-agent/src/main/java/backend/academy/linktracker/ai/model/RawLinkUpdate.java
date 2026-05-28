package backend.academy.linktracker.ai.model;

import java.util.List;

public record RawLinkUpdate(Long id, String author, String url, String description, List<Long> tgChatIds) {}
