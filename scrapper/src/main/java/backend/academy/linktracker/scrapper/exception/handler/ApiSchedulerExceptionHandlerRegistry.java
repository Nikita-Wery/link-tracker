// package backend.academy.linktracker.scrapper.exception.handler;
//
// import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Component;
//
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.function.Consumer;
//
// @Component
// public class ApiSchedulerExceptionHandlerRegistry {
//
//    private static final Logger log = LoggerFactory.getLogger(ApiSchedulerExceptionHandlerRegistry.class);
//
//    private final Map<Class<? extends ExternalApiException>, Consumer<ExternalApiException>> handlers = new
// HashMap<>();
//
//    public ApiSchedulerExceptionHandlerRegistry(List<ExternalApiException> externalApiExceptionList) {
//
////        handlers.put(LinkProcessingException.class, t ->
////                log.warn("LinkProcessingException caught in scheduler: {}", t.getMessage()));
////
////        handlers.put(IOException.class, t ->
////                log.error("IOException caught in scheduler", t));
//
//    }
//
//    public void handle(ExternalApiException ex) {
//        Consumer<ExternalApiException> handler = handlers.get(ex.getClass());
//        if (handler != null) {
//            handler.accept(ex);
//        } else {
//            log.error("Unhandled exception in scheduler", ex);
//        }
//    }
//
//    public void registerHandler(Class<? extends ExternalApiException> type, Consumer<ExternalApiException> handler) {
//        handlers.put(type, handler);
//    }
// }
