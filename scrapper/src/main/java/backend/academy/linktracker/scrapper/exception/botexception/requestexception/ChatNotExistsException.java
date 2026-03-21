package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;

public class ChatNotExistsException extends ScrapperApiException {

    private static final String CHAT_NOT_EXISTS_EXCEPTION = "Данный чат не существует";

    public ChatNotExistsException(String message) {
        super(message, CHAT_NOT_EXISTS_EXCEPTION);
    }
}
