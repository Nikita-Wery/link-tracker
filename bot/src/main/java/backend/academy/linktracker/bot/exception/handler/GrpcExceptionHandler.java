package backend.academy.linktracker.bot.exception.handler;

import backend.academy.linktracker.bot.exception.scrapperexception.requestexception.UnexpectedBotServerException;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.springframework.stereotype.Component;

@Component
public class GrpcExceptionHandler implements ServerInterceptor {

    private static final Metadata.Key<String> EXCEPTION_NAME =
            Metadata.Key.of("exception_name", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> EXCEPTION_MESSAGE =
            Metadata.Key.of("exception_message", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> DESCRIPTION =
            Metadata.Key.of("description", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (RuntimeException e) {
                    Metadata trailers = new Metadata();
                    trailers.put(EXCEPTION_NAME, e.getClass().getSimpleName());
                    trailers.put(EXCEPTION_MESSAGE, e.getMessage());

                    if (e instanceof UnexpectedBotServerException) {
                        trailers.put(DESCRIPTION, UnexpectedBotServerException.INTERNAL_BOT_SERVER_EXCEPTION);
                    } else {
                        trailers.put(DESCRIPTION, Status.INVALID_ARGUMENT.getDescription());
                    }

                    Status status =
                            e instanceof UnexpectedBotServerException ? Status.INTERNAL : Status.INVALID_ARGUMENT;

                    call.close(status, trailers);
                }
            }
        };
    }
}
