package backend.academy.linktracker.ai.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.client.KafkaBotClient;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.FilterProperties;
import backend.academy.linktracker.ai.properties.SummarizationProperties;
import backend.academy.linktracker.ai.service.filterimpl.ExcludedAuthorsFilter;
import backend.academy.linktracker.ai.service.filterimpl.MinLengthFilter;
import backend.academy.linktracker.ai.service.filterimpl.StopWordFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
class ValidRawLinkUpdateTest {

    FilterProperties filterProperties = new FilterProperties();

    SummarizationProperties summarizationProperties = new SummarizationProperties();

    SummarizationService summarizationService;

    List<RawLinkUpdateFilter> filters;

    @Mock
    KafkaBotClient kafkaBotClient;

    @Mock
    Acknowledgment acknowledgment;

    RawLinkUpdatesProcessor rawLinkUpdatesProcessor;

    @BeforeEach
    void setUp() {

        filterProperties.setStopWords(List.of("advertising", "casino", "spam"));

        filterProperties.setExcludedAuthors(List.of("bot"));

        filterProperties.setMinTextLength(2);

        filters = new ArrayList<>();

        filters.add(new ExcludedAuthorsFilter(filterProperties));

        filters.add(new MinLengthFilter(filterProperties));

        filters.add(new StopWordFilter(filterProperties));

        summarizationProperties.setMaxLength(50);

        summarizationService = new SummarizationService(summarizationProperties);

        rawLinkUpdatesProcessor = new RawLinkUpdatesProcessor(filters, summarizationService, kafkaBotClient);
    }

    @Test
    void shouldSendProcessedUpdate() {

        RawLinkUpdate update = new RawLinkUpdate(1L, "coolAuthor", "url", "valid description", List.of());

        when(kafkaBotClient.send(anyLong(), any())).thenReturn(CompletableFuture.completedFuture(null));

        rawLinkUpdatesProcessor.processRawLinkUpdate(1L, "topic", update, acknowledgment);

        verify(kafkaBotClient).send(anyLong(), any());

        verify(acknowledgment).acknowledge();
    }
}
