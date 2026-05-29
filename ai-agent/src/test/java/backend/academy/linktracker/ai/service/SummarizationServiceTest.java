package backend.academy.linktracker.ai.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.ai.model.LinkUpdateContext;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.SummarizationProperties;
import backend.academy.linktracker.ai.service.subprocessors.SummarizationProcessor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SummarizationServiceTest {

    SummarizationProcessor summarizationService;
    SummarizationProperties summarizationProperties = new SummarizationProperties();

    @BeforeEach
    public void setUp() {

        summarizationProperties.setMaxLength(10);

        summarizationService = new SummarizationProcessor(summarizationProperties);
    }

    @Test
    public void longMessage() {

        LinkUpdateContext linkUpdateContext = LinkUpdateContext.builder().build();

        RawLinkUpdate update = new RawLinkUpdate(1L, "user", "url", "*".repeat(50), List.of());

        linkUpdateContext.setRawLinkUpdate(update);

        summarizationService.process(linkUpdateContext);

        assertTrue(linkUpdateContext.getSummarizedDescription().length()
                == summarizationProperties.getMaxLength() + SummarizationProcessor.ELLIPSIS.length());
        assertEquals(
                linkUpdateContext.getSummarizedDescription(),
                "*".repeat(summarizationProperties.getMaxLength()) + SummarizationProcessor.ELLIPSIS);
    }
}
