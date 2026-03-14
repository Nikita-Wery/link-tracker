package backend.academy.linktracker.bot.utils;

import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DtoEntityMapper {

    public ApiErrorResponse buildApiErrorResponse(String description, String statusCode, Exception ex) {
        return ApiErrorResponse.builder()
                .description(description)
                .code(statusCode)
                .exceptionMessage(ex.getMessage())
                .exceptionName(ex.getClass().getSimpleName())
                .stackTrace(buildStacktrace(ex))
                .build();
    }

    public List<String> buildStacktrace(Exception exception) {
        return Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();
    }
}
