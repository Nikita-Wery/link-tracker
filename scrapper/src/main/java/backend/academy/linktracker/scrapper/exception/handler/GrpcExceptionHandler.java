package backend.academy.linktracker.scrapper.exception.handler;

import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.InvalidLinkInRequestException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotTrackedException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.UnexpectedScrapperServerException;
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
                Metadata trailers = new Metadata();

                try {
                    super.onHalfClose();
                } catch (ScrapperApiException e) {
                    trailers.put(EXCEPTION_NAME, e.getClass().getSimpleName());
                    trailers.put(EXCEPTION_MESSAGE, e.getMessage());
                    trailers.put(DESCRIPTION, e.getDescription());

                    Status status = mapToStatus(e);

                    call.close(status, trailers);
                } catch (RuntimeException e) {
                    trailers.put(EXCEPTION_NAME, UnexpectedScrapperServerException.class.getSimpleName());
                    trailers.put(EXCEPTION_MESSAGE, e.getMessage());

                    call.close(Status.INTERNAL, trailers);
                }
            }
        };
    }

    private Status mapToStatus(ScrapperApiException e) {

        return switch (e) {
            case InvalidLinkInRequestException ignored -> Status.INVALID_ARGUMENT;
            case ChatNotExistsException ignored -> Status.NOT_FOUND;
            case LinkNotTrackedException ignored -> Status.NOT_FOUND;
            case LinkAlreadyTrackedException ignored -> Status.ALREADY_EXISTS;
            case ChatAlreadyExistsException ignored -> Status.ALREADY_EXISTS;
            case LinkNotExistsException ignored -> Status.INTERNAL;
            default -> Status.INTERNAL;
        };
    }
}
