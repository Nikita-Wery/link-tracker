package backend.academy.linktracker.scrapper.utils;

import backend.academy.linktracker.scrapper.config.ResourceType;
import java.util.Arrays;
import java.util.Optional;

public class ResourceTypeMapper {

    public static Optional<ResourceType> getResourceTypeFromURI(String url) {
        return Arrays.stream(ResourceType.values())
                .filter(req -> req.parser().supports(url))
                .findFirst();
    }
}
