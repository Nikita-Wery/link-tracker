package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;

public class ChatAlreadyExistsException extends ScrapperApiException {

    private static final String CHAT_ALREADY_EXISTS_EXCEPTION = "Данный чат уже существует";

    public ChatAlreadyExistsException(String message) {
        super(message, CHAT_ALREADY_EXISTS_EXCEPTION);
    }
}
