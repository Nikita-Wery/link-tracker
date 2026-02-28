package backend.academy.linktracker.bot.configuration.logging;

import static backend.academy.linktracker.bot.configuration.logging.ConsoleLoggerConfig.LogPrefix.AFTER_RETURNING;
import static backend.academy.linktracker.bot.configuration.logging.ConsoleLoggerConfig.LogPrefix.AFTER_THROWING;
import static backend.academy.linktracker.bot.configuration.logging.ConsoleLoggerConfig.LogPrefix.BEFORE;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Консольный logger
 *
 * @author LuzinNikita
 */
@Aspect
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "app.logger.console", value = "enabled", havingValue = "true", matchIfMissing = true)
public class ConsoleLoggerConfig {

    private static final String BEFORE_METHOD_MESSAGE_PATTERN =
            "%s method [{}] call args [{}]".formatted(BEFORE.getLogPrefix());

    private static final String AFTER_METHOD_MESSAGE_PATTERN =
            "%s method [{}] result: [{}]".formatted(AFTER_RETURNING.getLogPrefix());

    private static final String AFTER_THROWING_MESSAGE_PATTERN =
            "%s method [{}] throw exception".formatted(AFTER_THROWING.getLogPrefix());

    @Before(value = "backend.academy.linktracker.bot.logging.aspect.SystemArchitecture.allMethods()")
    public void beforeLogControllers(JoinPoint joinPoint) {
        log.info(BEFORE_METHOD_MESSAGE_PATTERN, joinPoint.getSignature(), joinPoint.getArgs());
    }

    @AfterReturning(
            value = "backend.academy.linktracker.bot.logging.aspect.SystemArchitecture.allMethods()",
            returning = "result")
    public void afterReturningLogController(JoinPoint joinPoint, Object result) {
        log.info(AFTER_METHOD_MESSAGE_PATTERN, joinPoint.getSignature(), result);
    }

    @AfterThrowing(
            value = "backend.academy.linktracker.bot.logging.aspect.SystemArchitecture.allMethods()",
            throwing = "exception")
    public void afterLogControllers(JoinPoint joinPoint, Throwable exception) {
        log.error(AFTER_THROWING_MESSAGE_PATTERN, joinPoint.getSignature(), exception);
    }

    public enum LogPrefix {
        BEFORE("====@Before===="),
        AFTER_RETURNING("====@AfterReturning===="),
        AFTER_THROWING("====@AfterThrowing====");

        LogPrefix(String logPrefix) {
            this.logPrefix = logPrefix;
        }

        final String logPrefix;

        public String getLogPrefix() {
            return logPrefix;
        }
    }
}
