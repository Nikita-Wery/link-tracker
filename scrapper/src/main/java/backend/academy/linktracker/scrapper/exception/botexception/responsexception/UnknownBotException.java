package backend.academy.linktracker.scrapper.exception.botexception.responsexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class UnknownBotException extends BotApiException {
    public UnknownBotException(String message) {
        super(message);
    }
}
