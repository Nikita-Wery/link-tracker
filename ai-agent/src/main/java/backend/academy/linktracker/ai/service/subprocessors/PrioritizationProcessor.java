package backend.academy.linktracker.ai.service.subprocessors;

import backend.academy.linktracker.ai.model.LinkUpdateContext;
import backend.academy.linktracker.ai.model.Priority;
import backend.academy.linktracker.ai.properties.PrioritizationProperties;
import backend.academy.linktracker.ai.service.SubProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrioritizationProcessor implements SubProcessor<LinkUpdateContext> {

    private final PrioritizationProperties prioritizationProperties;

    @Override
    public void process(LinkUpdateContext context) {

        for (String keyword : prioritizationProperties.getHighKeywords()) {

            if (context.getRawLinkUpdate().description().toLowerCase().contains(keyword.toLowerCase())) {

                log.info(
                        "Set HIGH priority to LinkUpdate with id {}",
                        context.getRawLinkUpdate().id());

                context.setPriority(Priority.HIGH);
                return;
            }
        }

        for (String keyword : prioritizationProperties.getLowKeywords()) {

            if (context.getRawLinkUpdate().description().toLowerCase().contains(keyword.toLowerCase())) {

                log.info(
                        "Set LOW priority to LinkUpdate with id {}",
                        context.getRawLinkUpdate().id());

                context.setPriority(Priority.LOW);
                return;
            }
        }

        log.info(
                "Set MEDIUM priority to LinkUpdate with id {}",
                context.getRawLinkUpdate().id());

        context.setPriority(Priority.MEDIUM);
    }
}
