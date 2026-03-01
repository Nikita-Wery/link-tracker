package backend.academy.linktracker.bot.configuration.constants;

/**
 * Наиболее частые key при логировании
 *
 * @author Luzin Nikita
 */
public interface LoggerConstant {

    String HTTP_HEADERS_REQUEST_ID = "requestId";

    String STRUCTURED_ARGUMENTS_KEY_SERVICE = "service";

    String STRUCTURED_ARGUMENTS_KEY_METHOD = "method";

    String STRUCTURED_ARGUMENTS_KEY_ARGS = "args";

    String STRUCTURED_ARGUMENTS_KEY_RESULT = "result";

    String STRUCTURED_ARGUMENTS_KEY_STEP = "loggingStep";

    String STRUCTURED_ARGUMENTS_KEY_EXCEPTION = "exception";

    String STRUCTURED_ARGUMENTS_KEY_REQUEST_URI = "requestURI";

    String STRUCTURED_ARGUMENTS_KEY_HTTP_RESPONSE_STATUS_CODE = "httpResponseStatusCode";

    String STRUCTURED_ARGUMENTS_KEY_COMMAND_NAME = "commandName";

    String STRUCTURED_ARGUMENTS_KEY_EVENT = "event";

    String STRUCTURED_ARGUMENTS_KEY_CHAT_ID = "chatId";

    String STRUCTURED_ARGUMENTS_KEY_MESSAGE = "message";

    interface Step {

        String BEFORE_CONTROLLER = "BEFORE controller calling";

        String AFTER_CONTROLLER = "AFTER controller calling";

        String BEFORE_METHOD = "BEFORE method calling";

        String AFTER_METHOD = "AFTER method calling";

        String AFTER_THROWING = "AFTER exception throwing";

        String AFTER_COMPLETION = "AFTER request processed";

        String BEFORE_EVENT_LISTENER = "BEFORE event listener";

        String AFTER_EVENT_LISTENER = "AFTER event listener";

        String BEFORE_METHOD_LOGGING_METHOD = "BEFORE methodLogging";

        String AFTER_METHOD_LOGGING_METHOD = "AFTER methodLogging";
    }
}
