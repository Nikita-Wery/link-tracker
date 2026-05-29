package backend.academy.linktracker.ai.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Priority {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    private final int weight;
}
