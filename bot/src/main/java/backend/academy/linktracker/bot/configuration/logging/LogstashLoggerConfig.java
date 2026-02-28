package backend.academy.linktracker.bot.configuration.logging;

import static backend.academy.linktracker.bot.configuration.constants.LoggerConstant.*;
import static backend.academy.linktracker.bot.configuration.constants.LoggerConstant.Step.AFTER_METHOD;
import static net.logstash.logback.argument.StructuredArguments.value;

import backend.academy.linktracker.bot.application.command.Command;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Файловый логгер
 * может быть легко перенастроен на запись в socket
 *
 * @author LuzinNikita
 */
@Slf4j
@Aspect
@Configuration
@ConditionalOnProperty(prefix = "app.logger.file", value = "enabled", havingValue = "true", matchIfMissing = true)
public class LogstashLoggerConfig {

    @Before("backend.academy.linktracker.bot.logging.aspect.SystemArchitecture.loggingEntryPoints()")
    public void beforeLogging(JoinPoint joinPoint) {
        String requestId = Optional.ofNullable(MDC.get(HTTP_HEADERS_REQUEST_ID))
                .orElse(UUID.randomUUID().toString());

        MDC.put(HTTP_HEADERS_REQUEST_ID, requestId);
    }

    @After("backend.academy.linktracker.bot.logging.aspect.SystemArchitecture.commandHandleMethod()"
            + " && target(command)")
    public void addCommandName(JoinPoint joinPoint, Command command) {
        MDC.put(STRUCTURED_ARGUMENTS_KEY_COMMAND_NAME, command.getCommandName());
    }

    @Before("backend.academy.linktracker.bot.logging.aspect.SystemArchitecture.sendTelegramMessageMethod()"
            + " && args(chatId, message)")
    public void addChatId(JoinPoint joinPoint, Long chatId, String message) {
        MDC.put(STRUCTURED_ARGUMENTS_KEY_CHAT_ID, String.valueOf(chatId));
    }

    @After("backend.academy.linktracker.bot.logging.aspect.SystemArchitecture.sendTelegramMessageMethod()")
    public void afterSendTelegramMethod() {
        log.info(AFTER_METHOD, value(STRUCTURED_ARGUMENTS_KEY_SERVICE, "bot-service"), value("event", "call comand"));
    }
}
