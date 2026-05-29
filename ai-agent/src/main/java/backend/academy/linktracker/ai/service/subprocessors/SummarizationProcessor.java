package backend.academy.linktracker.ai.service.subprocessors;

import backend.academy.linktracker.ai.model.LinkUpdateContext;
import backend.academy.linktracker.ai.properties.SummarizationProperties;
import backend.academy.linktracker.ai.service.SubProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummarizationProcessor implements SubProcessor<LinkUpdateContext> {

    public static final String ELLIPSIS = "...";
    private final SummarizationProperties properties;

    @Override
    public void process(LinkUpdateContext context) {

        String summarizedDescription = context.getRawLinkUpdate().description();

        if (summarizedDescription.length() >= properties.getMaxLength()) {
            summarizedDescription = summarizedDescription.substring(0, properties.getMaxLength()) + ELLIPSIS;
        }

        context.setSummarizedDescription(summarizedDescription);
    }
}
