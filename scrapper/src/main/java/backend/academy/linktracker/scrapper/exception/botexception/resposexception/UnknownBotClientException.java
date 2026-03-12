package backend.academy.linktracker.scrapper.exception.botexception.resposexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class UnknownBotClientException extends BotApiException {
    public UnknownBotClientException(String message) {
        super(message);
    }
}
