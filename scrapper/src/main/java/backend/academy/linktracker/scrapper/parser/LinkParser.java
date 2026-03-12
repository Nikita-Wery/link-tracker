package backend.academy.linktracker.scrapper.parser;

import java.net.URI;

public interface LinkParser<T> {

    boolean supports(String url);

    T parse(URI url);
}
