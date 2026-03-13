package backend.academy.linktracker.scrapper.exception.botexception.responsexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class InvalidPropertyInUpdateException extends BotApiException {
    public InvalidPropertyInUpdateException(String message) {
        super(message);
    }
}
