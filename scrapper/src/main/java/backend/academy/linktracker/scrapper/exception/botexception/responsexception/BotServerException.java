package backend.academy.linktracker.scrapper.exception.botexception.responsexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class BotServerException extends BotApiException {
    public BotServerException(String message) {
        super(message);
    }
}
