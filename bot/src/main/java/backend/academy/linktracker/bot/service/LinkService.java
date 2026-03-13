package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.client.ScrapperClient;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.Set;

@Service
public class LinkService {

    public final ScrapperClient scrapperClient;

    public LinkService(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    public void save(Long chatId, URI url, Set<String> tags) {

    }
}
