package backend.academy.linktracker.scrapper.exception.botexception.responsexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class UnknownBotClientException extends BotApiException {
    public UnknownBotClientException(String message) {
        super(message);
    }
}
