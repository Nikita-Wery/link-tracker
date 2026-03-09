package backend.academy.linktracker.scrapper.mapper;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {

    public LinkResponse linkToLinkResponse(Link link) {
        return new LinkResponse(
            link.getId(),
            link.getUrl(),
            link.getTags().stream().toList(),
            link.getFilters().stream().toList());
    }

}
