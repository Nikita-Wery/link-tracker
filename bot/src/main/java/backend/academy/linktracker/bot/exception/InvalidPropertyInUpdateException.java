package backend.academy.linktracker.bot.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;

public class InvalidPropertyInUpdateException extends MethodArgumentNotValidException {
    public InvalidPropertyInUpdateException(MethodArgumentNotValidException ex) {
        super(ex.getParameter(), ex.getBindingResult());
    }
}
