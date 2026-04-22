package backend.academy.linktracker.scrapper.dto.stackoverflow;

import java.util.List;

public record StackOverflowWrapper<T>(List<T> items) {}
