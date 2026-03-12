package backend.academy.linktracker.scrapper.exception.handler;

import backend.academy.linktracker.scrapper.dto.bot.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.InvalidLinkInRequestException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotSupportedException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotTrackedException;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private final String CHAT_ALREADY_EXISTS_EXCEPTION = "Данный чат уже существует";
    private final String CHAT_NOT_EXISTS_EXCEPTION = "Данный чат не существует";
    private final String LINK_ALREADY_TRACKED_EXCEPTION = "Данная ссылка уже отслеживается пользователем";
    private final String LINK_NOT_SUPPORTED_EXCEPTION = "Данная ссылка сейчас не поддерживается для отслеживания";
    private final String LINK_NOT_TRACKED_EXCEPTION = "Данная ссыка не отслеживается на даный момент";
    private final String NULL_POINTER_EXCEPTION = "Entity было создано без необходимого свойства";

    private final DtoEntityMapper dtoEntityMapper;

    public GlobalExceptionHandler(DtoEntityMapper dtoEntityMapper) {
        this.dtoEntityMapper = dtoEntityMapper;
    }

    @ExceptionHandler(ChatAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> chatAlreadyExists(ChatAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            dtoEntityMapper.buildApiErrorResponse(
                CHAT_ALREADY_EXISTS_EXCEPTION,
                Integer.toString(HttpStatus.CONFLICT.value()),
                ex)
        );
    }

    @ExceptionHandler(ChatNotExistsException.class)
    public ResponseEntity<ApiErrorResponse> chatNotExists(ChatNotExistsException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            dtoEntityMapper.buildApiErrorResponse(
                CHAT_NOT_EXISTS_EXCEPTION,
                Integer.toString(HttpStatus.NOT_FOUND.value()),
                ex)
        );
    }

    @ExceptionHandler(LinkAlreadyTrackedException.class)
    public ResponseEntity<ApiErrorResponse> linkAlreadyTracked(LinkAlreadyTrackedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            dtoEntityMapper.buildApiErrorResponse(
                LINK_ALREADY_TRACKED_EXCEPTION,
                Integer.toString(HttpStatus.CONFLICT.value()),
                ex)
        );
    }

    @ExceptionHandler(LinkNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> linkNotSupported(LinkNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            dtoEntityMapper.buildApiErrorResponse(
                LINK_NOT_SUPPORTED_EXCEPTION,
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                ex)
        );
    }

    @ExceptionHandler(LinkNotTrackedException.class)
    public ResponseEntity<ApiErrorResponse> linkNotTracked(LinkNotTrackedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            dtoEntityMapper.buildApiErrorResponse(
                LINK_NOT_TRACKED_EXCEPTION,
                Integer.toString(HttpStatus.NOT_FOUND.value()),
                ex)
        );
    }

    @ExceptionHandler({LinkNotExistsException.class, ChatNotExistsException.class})
    public ResponseEntity<String> linkNotExists(RuntimeException ex) {
        return new ResponseEntity<>(
             HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> nullPointerException(NullPointerException ex) {
        return new ResponseEntity<>(
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()
            + " " + NULL_POINTER_EXCEPTION + ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(InvalidLinkInRequestException.class)
    public ResponseEntity<String> invalidLinkInRequestException(InvalidLinkInRequestException ex) {
        return new ResponseEntity<>(
            HttpStatus.BAD_REQUEST.getReasonPhrase()
            + " " + ex.getMessage(),
            HttpStatus.BAD_REQUEST
        );
    }

}
