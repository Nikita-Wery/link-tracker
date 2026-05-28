package backend.academy.linktracker.ai.service.filterimpl;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.FilterProperties;
import backend.academy.linktracker.ai.service.RawLinkUpdateFilter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExcludedAuthorsFilter implements RawLinkUpdateFilter {

    private final FilterProperties properties;

    @Override
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public boolean filter(RawLinkUpdate rawLinkUpdate) {

        boolean excludedAuthorsNotFound = !properties.getExcludedAuthors().contains(rawLinkUpdate.author());

        if (!excludedAuthorsNotFound) {
            log.info(
                    "RawLinkUpdate rejected, excluded author",
                    kv("updateId", rawLinkUpdate.id()),
                    kv("excluded_author", rawLinkUpdate.author()));
        }

        return excludedAuthorsNotFound;
    }
}
