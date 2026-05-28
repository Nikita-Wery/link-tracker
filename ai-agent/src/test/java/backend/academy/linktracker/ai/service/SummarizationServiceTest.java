package backend.academy.linktracker.ai.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.ai.properties.SummarizationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SummarizationServiceTest {

    SummarizationService summarizationService;
    SummarizationProperties summarizationProperties = new SummarizationProperties();

    @BeforeEach
    public void setUp() {

        summarizationProperties.setMaxLength(10);

        summarizationService = new SummarizationService(summarizationProperties);
    }

    @Test
    public void longMessage() {

        String message = "*".repeat(50);

        String resultMessage = summarizationService.summarizeDescription(message);

        assertTrue(resultMessage.length()
                == summarizationProperties.getMaxLength() + SummarizationService.ELLIPSIS.length());
        assertEquals(resultMessage, "*".repeat(summarizationProperties.getMaxLength()) + SummarizationService.ELLIPSIS);
    }
}
