package backend.academy.linktracker.scrapper.exception.botexception;

import lombok.Getter;

@Getter
public class ScrapperApiException extends RuntimeException {

    private final String description;

    public ScrapperApiException(String message, String description) {
        this.description = description;
        super(message);
    }
}
