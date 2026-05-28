package backend.academy.linktracker.ai.service.filterimpl;

import static org.junit.jupiter.api.Assertions.assertFalse;

import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.FilterProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExcludedAuthorsFilterTest {

    private ExcludedAuthorsFilter excludedAuthorsFilter;

    @BeforeEach
    void setUp() {

        FilterProperties properties = new FilterProperties();

        properties.setExcludedAuthors(List.of("bot"));

        excludedAuthorsFilter = new ExcludedAuthorsFilter(properties);
    }

    @Test
    void illegalAuthor() {

        RawLinkUpdate update = new RawLinkUpdate(1L, "bot", "url", "description", List.of());

        assertFalse(excludedAuthorsFilter.filter(update));
    }
}
