package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;

public class LinkNotExistsException extends ScrapperApiException {

    private static final String LINK_NOT_EXISTS = "Общий шаблон ссылки не существует";

    public LinkNotExistsException(String message) {
        super(message, LINK_NOT_EXISTS);
    }
}
