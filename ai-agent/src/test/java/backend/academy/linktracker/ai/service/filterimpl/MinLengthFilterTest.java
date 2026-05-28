package backend.academy.linktracker.ai.service.filterimpl;

import static org.junit.jupiter.api.Assertions.assertFalse;

import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.FilterProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MinLengthFilterTest {

    MinLengthFilter minLengthFilter;

    @BeforeEach
    void setUp() {

        FilterProperties properties = new FilterProperties();

        properties.setMinTextLength(2);

        minLengthFilter = new MinLengthFilter(properties);
    }

    @Test
    public void tooShortMessage() {

        RawLinkUpdate update = new RawLinkUpdate(1L, "bot", "url", "x", List.of());

        assertFalse(minLengthFilter.filter(update));
    }
}
