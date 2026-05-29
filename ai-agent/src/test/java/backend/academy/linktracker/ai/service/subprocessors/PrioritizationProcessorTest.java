package backend.academy.linktracker.ai.service.subprocessors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.ai.model.LinkUpdateContext;
import backend.academy.linktracker.ai.model.Priority;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.PrioritizationProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrioritizationProcessorTest {

    PrioritizationProperties prioritizationProperties;

    PrioritizationProcessor prioritizationProcessor;

    @BeforeEach
    public void setup() {
        prioritizationProperties = new PrioritizationProperties();

        prioritizationProperties.setHighKeywords(List.of("bug", "security", "critical"));
        prioritizationProperties.setLowKeywords(List.of("minor", "typo"));

        prioritizationProcessor = new PrioritizationProcessor(prioritizationProperties);
    }

    @Test
    public void highKeywordFound_shouldSetHighPriority() {

        LinkUpdateContext linkUpdateContext = LinkUpdateContext.builder().build();

        String descr = "That very cool, and also critical to..";
        String descr2 = "Lorem ipsum dollar bUg";
        String descr3 = "Its something special andSecurIty";

        RawLinkUpdate update = new RawLinkUpdate(1L, "user", "url", descr, List.of());
        RawLinkUpdate update2 = new RawLinkUpdate(2L, "user", "url", descr2, List.of());
        RawLinkUpdate update3 = new RawLinkUpdate(3L, "user", "url", descr3, List.of());

        linkUpdateContext.setRawLinkUpdate(update);
        prioritizationProcessor.process(linkUpdateContext);

        assertTrue(linkUpdateContext.getPriority().equals(Priority.HIGH));

        linkUpdateContext.setPriority(Priority.LOW);
        linkUpdateContext.setRawLinkUpdate(update2);
        prioritizationProcessor.process(linkUpdateContext);

        assertTrue(linkUpdateContext.getPriority().equals(Priority.HIGH));

        linkUpdateContext.setPriority(Priority.LOW);
        linkUpdateContext.setRawLinkUpdate(update3);
        prioritizationProcessor.process(linkUpdateContext);

        assertTrue(linkUpdateContext.getPriority().equals(Priority.HIGH));
    }

    @Test
    public void lowKeywordFound_shouldSetLowPriority() {

        LinkUpdateContext linkUpdateContext = LinkUpdateContext.builder().build();

        String descr = "That very minocool, and also critypoical to..";
        String descr2 = "Lorem ipsum dollar miNor";
        String descr3 = "Its something special andTypo";

        RawLinkUpdate update = new RawLinkUpdate(1L, "user", "url", descr, List.of());
        RawLinkUpdate update2 = new RawLinkUpdate(2L, "user", "url", descr2, List.of());
        RawLinkUpdate update3 = new RawLinkUpdate(3L, "user", "url", descr3, List.of());

        linkUpdateContext.setRawLinkUpdate(update);
        prioritizationProcessor.process(linkUpdateContext);

        assertTrue(linkUpdateContext.getPriority().equals(Priority.LOW));

        linkUpdateContext.setPriority(Priority.HIGH);
        linkUpdateContext.setRawLinkUpdate(update2);
        prioritizationProcessor.process(linkUpdateContext);

        assertTrue(linkUpdateContext.getPriority().equals(Priority.LOW));

        linkUpdateContext.setPriority(Priority.HIGH);
        linkUpdateContext.setRawLinkUpdate(update3);
        prioritizationProcessor.process(linkUpdateContext);

        assertTrue(linkUpdateContext.getPriority().equals(Priority.LOW));
    }

    @Test
    public void highAndLowWordsNotFound_shouldSetMediumPriority() {

        LinkUpdateContext linkUpdateContext = LinkUpdateContext.builder().build();

        String descrLOW = "That very minocool, and also critypoical to..";
        String descrHIGH = "Lorem ipsum dollar miNor";
        String descrMEDUIM = "There is no high and low word here, it should be medium";

        RawLinkUpdate update = new RawLinkUpdate(1L, "user", "url", descrLOW, List.of());
        RawLinkUpdate update2 = new RawLinkUpdate(2L, "user", "url", descrHIGH, List.of());
        RawLinkUpdate update3 = new RawLinkUpdate(3L, "user", "url", descrMEDUIM, List.of());

        linkUpdateContext.setRawLinkUpdate(update);
        prioritizationProcessor.process(linkUpdateContext);

        assertFalse(linkUpdateContext.getPriority().equals(Priority.MEDIUM));

        linkUpdateContext.setRawLinkUpdate(update2);
        prioritizationProcessor.process(linkUpdateContext);

        assertFalse(linkUpdateContext.getPriority().equals(Priority.MEDIUM));

        linkUpdateContext.setPriority(Priority.HIGH);
        linkUpdateContext.setRawLinkUpdate(update3);
        prioritizationProcessor.process(linkUpdateContext);

        assertTrue(linkUpdateContext.getPriority().equals(Priority.MEDIUM));
    }
}
