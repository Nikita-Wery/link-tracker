package backend.academy.linktracker.ai.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.FilterProperties;
import backend.academy.linktracker.ai.service.filterimpl.ExcludedAuthorsFilter;
import backend.academy.linktracker.ai.service.filterimpl.MinLengthFilter;
import backend.academy.linktracker.ai.service.filterimpl.StopWordFilter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class ValidRawLinkUpdateTest {

    FilterProperties filterProperties = new FilterProperties();

    List<RawLinkUpdateFilter> filters;

    @Mock
    Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {

        RawLinkUpdate update = new RawLinkUpdate(1L, "user", "url", "*".repeat(50), List.of());

        filterProperties.setStopWords(List.of("advertising", "casino", "spam"));
        filterProperties.setExcludedAuthors(List.of("bot"));
        filterProperties.setMinTextLength(2);
        filters = new ArrayList<>();

        filters.add(new ExcludedAuthorsFilter(filterProperties));
        filters.add(new MinLengthFilter(filterProperties));
        filters.add(new StopWordFilter(filterProperties));
    }

    @Test
    void shouldAcceptAllFilters() {

        RawLinkUpdate update = new RawLinkUpdate(1L, "coolAuthor", "url", "valid description", List.of());

        assertTrue(filters.stream().allMatch(f -> f.filter(update)));
    }
}
