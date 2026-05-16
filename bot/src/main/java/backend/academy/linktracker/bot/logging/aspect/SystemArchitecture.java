package backend.academy.linktracker.bot.logging.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Хранилище всех jointPoint
 *
 * @author Luzin Nikita
 */
@Aspect
@Component
public class SystemArchitecture {

    @Pointcut("execution(* backend.academy..security..*(..))")
    public void securityMethods() {}

    @Pointcut("within(backend.academy..*) && !securityMethods()")
    public void allMethods() {}

    @Pointcut("@annotation(backend.academy.linktracker.bot.utils.annotations.MethodLogging)")
    public void methodLoggingMethods() {}

    @Pointcut("execution(* backend..UpdateDispatcher.dispatch(com.pengrad.telegrambot.model.Update))")
    public void dispatchMethod() {}

    @Pointcut("execution(* backend..Command.handle(..))")
    public void commandHandleMethod() {}

    @Pointcut("execution(* backend..TelegramMessageSender.sendMessage(Long, String))")
    public void sendTelegramMessageMethod() {}

    @Pointcut("dispatchMethod() || methodLoggingMethods()")
    public void loggingEntryPoints() {}
}
