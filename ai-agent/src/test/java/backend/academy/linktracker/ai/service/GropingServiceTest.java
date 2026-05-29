package backend.academy.linktracker.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import backend.academy.linktracker.ai.model.Priority;
import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GropingServiceTest {

    @InjectMocks
    private GropingService service;

    @Test
    void TC21_shouldGroupMultipleUpdatesIntoSingleMessage() {

        ProcessedLinkUpdate u1 = new ProcessedLinkUpdate(1L, "url", "first", List.of(123L), Priority.LOW);

        ProcessedLinkUpdate u2 = new ProcessedLinkUpdate(1L, "url", "second", List.of(123L), Priority.HIGH);

        List<ProcessedLinkUpdate> updates = List.of(u1, u2);

        ProcessedLinkUpdate result = service.group(updates);

        assertEquals(Priority.HIGH, result.priority());
        assertEquals("1. first\n2. second\n", result.description());
        assertEquals(1L, result.id());
    }

    @Test
    void TC22_shouldNotGroupSingleUpdate() {

        ProcessedLinkUpdate update = new ProcessedLinkUpdate(1L, "url", "only-one", List.of(123L), Priority.HIGH);

        List<ProcessedLinkUpdate> updates = List.of(update);

        ProcessedLinkUpdate result = service.group(updates);

        assertSame(update, result);
        assertEquals("only-one", result.description());
        assertEquals(Priority.HIGH, result.priority());
        assertEquals("url", result.url());
    }
}
