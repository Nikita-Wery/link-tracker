package backend.academy.linktracker.scrapper.controller.grpc;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.proto.AddLinkRequest;
import backend.academy.linktracker.proto.GetLinksRequest;
import backend.academy.linktracker.proto.LinkResponse;
import backend.academy.linktracker.proto.ListLinksResponse;
import backend.academy.linktracker.proto.RegisterChatRequest;
import backend.academy.linktracker.proto.RemoveChatRequest;
import backend.academy.linktracker.proto.RemoveLinkRequest;
import backend.academy.linktracker.proto.ScrapperServiceGrpc;
import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.service.ChatService;
import backend.academy.linktracker.scrapper.service.subscriptionimpl.SubscriptionServiceBase;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import backend.academy.linktracker.scrapper.utils.GrpcMapper;
import com.google.protobuf.Empty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.grpc.stub.StreamObserver;
import java.util.HashSet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
@AllArgsConstructor
public class ScrapperGrpcEndpoint extends ScrapperServiceGrpc.ScrapperServiceImplBase {

    private final ChatService chatService;
    private final SubscriptionServiceBase subscriptionServiceBase;
    private final DtoEntityMapper dtoEntityMapper;
    private final GrpcMapper grpcMapper;

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @Override
    public void registerChat(RegisterChatRequest req, StreamObserver<Empty> responseObserver) {
        log.info("chat_registration", kv("chat_id", req.getChatId()));

        Chat chat = dtoEntityMapper.getChatFromChatId(req.getChatId());
        chatService.addChat(chat);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @Override
    public void removeChat(RemoveChatRequest req, StreamObserver<Empty> responseObserver) {
        log.info("deleting_chat", kv("chat_id", req.getChatId()));

        chatService.deleteChatById(req.getChatId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @Override
    public void getLinks(GetLinksRequest req, StreamObserver<ListLinksResponse> responseObserver) {
        log.info("get_links", kv("chat_id", req.getChatId()));

        ListLinksResponse linksResponse = subscriptionServiceBase.getProtoListLinksResponseByChatId(req.getChatId());

        responseObserver.onNext(linksResponse);
        responseObserver.onCompleted();
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @Override
    public void addLink(AddLinkRequest grpcreq, StreamObserver<LinkResponse> responseObserver) {
        log.info("track_link", kv("chat_id", grpcreq.getChatId()), kv("link_url", grpcreq.getUrl()));

        backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest request = grpcMapper.mapAddLinkRequest(grpcreq);

        Chat chat = dtoEntityMapper.getChatFromChatId(grpcreq.getChatId());
        Link link = dtoEntityMapper.linkFromAddLinkRequest(request);
        ChatLink chatLink = new ChatLink(link, chat, new HashSet<>(request.tags()));

        LinkResponse linkResponse = subscriptionServiceBase.trackLinkReturnProtoLinkResponse(chatLink);

        responseObserver.onNext(linkResponse);
        responseObserver.onCompleted();
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @Override
    public void deleteLink(RemoveLinkRequest grpcreq, StreamObserver<LinkResponse> responseObserver) {
        log.info("untrack_link", kv("chat_id", grpcreq.getChatId()), kv("link_url", grpcreq.getLink()));

        backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest request =
                grpcMapper.mapRemoveLinkRequest(grpcreq);

        Chat chat = dtoEntityMapper.getChatFromChatId(grpcreq.getChatId());
        Link link = dtoEntityMapper.linkFromRemoveLinkRequest(request);
        ChatLink chatLink = new ChatLink(link, chat);

        LinkResponse linkResponse = subscriptionServiceBase.untrackLinkReturnProtoLinkResponse(chatLink);

        responseObserver.onNext(linkResponse);
        responseObserver.onCompleted();
    }
}
