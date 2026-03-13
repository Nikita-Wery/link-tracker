package backend.academy.linktracker.bot.exception.handler;

import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.exception.scrapperexception.requestexception.InvalidLinkUpdateException;
import backend.academy.linktracker.bot.exception.scrapperexception.requestexception.InvalidPropertyInUpdateException;
import backend.academy.linktracker.bot.utils.DtoEntityMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private final String READING_REQUEST_BODY_EXCEPTION = "Не получилось сопоставить класс и тело запроса";
    private final String READING_FIELD_IN_REQUEST_EXCEPTION = "Не получилось сопоставить поле класса и тело запроса";

    private final DtoEntityMapper dtoEntityMapper;

    public GlobalExceptionHandler(DtoEntityMapper dtoEntityMapper) {
        this.dtoEntityMapper = dtoEntityMapper;
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleRequestBodyMismatch(HttpMessageNotReadableException ex) {
        ex = new InvalidLinkUpdateException(ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
              dtoEntityMapper.buildApiErrorResponse(
                READING_REQUEST_BODY_EXCEPTION,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                ex
            )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleRequestFileMismatch(MethodArgumentNotValidException ex) {
        ex = new InvalidPropertyInUpdateException(ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            dtoEntityMapper.buildApiErrorResponse(
                READING_FIELD_IN_REQUEST_EXCEPTION,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                ex
            )
        );
    }

}
