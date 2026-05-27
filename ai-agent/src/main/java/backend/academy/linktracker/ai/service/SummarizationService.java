package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.properties.SummarizationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummarizationService {

    private static final String ELLIPSIS = "...";
    private final SummarizationProperties properties;

    public String summarizeDescription(String description) {

        String resultDescription = description;

        if (resultDescription.length() >= properties.getMaxLength()) {
            resultDescription = resultDescription.substring(0, properties.getMaxLength()) + ELLIPSIS;
        }

        return resultDescription;
    }

}
