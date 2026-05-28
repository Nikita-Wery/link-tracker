package backend.academy.linktracker.ai.service.filterimpl;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.FilterProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StopWordFilterTest {

    StopWordFilter stopWordFilter;

    @BeforeEach
    public void setUp() {

        FilterProperties properties = new FilterProperties();

        properties.setStopWords(List.of("casino", "spam", "advertising"));

        stopWordFilter = new StopWordFilter(properties);
    }

    @Test
    void illegalWord() {

        RawLinkUpdate update = new RawLinkUpdate(1L, "bot", "url", "Hm, what about advertising", List.of());

        RawLinkUpdate update2 = new RawLinkUpdate(2L, "bot", "url", "Oh, this is lorem ipsum with casino", List.of());

        RawLinkUpdate update3 =
                new RawLinkUpdate(3L, "bot", "url", "This message havespam and should be reject", List.of());

        assertFalse(stopWordFilter.filter(update));
        assertFalse(stopWordFilter.filter(update2));
        assertFalse(stopWordFilter.filter(update3));
    }
}
