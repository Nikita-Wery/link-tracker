package backend.academy.linktracker.scrapper.controller.grpc;

import backend.academy.linktracker.proto.*;
import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.service.ChatService;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.SubscriptionService;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import backend.academy.linktracker.scrapper.utils.GrpcMapper;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.HashSet;
import lombok.AllArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@AllArgsConstructor
public class ScrapperGrpcEndpoint extends ScrapperServiceGrpc.ScrapperServiceImplBase {

    private final ChatService chatService;
    private final LinkService linkService;
    private final SubscriptionService subscriptionService;
    private final DtoEntityMapper dtoEntityMapper;
    private final GrpcMapper grpcMapper;

    @Override
    public void registerChat(RegisterChatRequest req, StreamObserver<Empty> responseObserver) {
        Chat chat = dtoEntityMapper.getChatFromChatId(req.getChatId());
        chatService.addChat(chat);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void removeChat(RemoveChatRequest req, StreamObserver<Empty> responseObserver) {
        chatService.deleteChatById(req.getChatId());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getLinks(GetLinksRequest req, StreamObserver<ListLinksResponse> responseObserver) {
        ListLinksResponse linksResponse = subscriptionService.getProtoListLinksResponseByChatId(req.getChatId());

        responseObserver.onNext(linksResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void addLink(AddLinkRequest grpcreq, StreamObserver<LinkResponse> responseObserver) {
        backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest request = grpcMapper.mapAddLinkRequest(grpcreq);

        Chat chat = dtoEntityMapper.getChatFromChatId(grpcreq.getChatId());
        Link link = dtoEntityMapper.linkFromAddLinkRequest(request);
        ChatLink chatLink = new ChatLink(link, chat, new HashSet<>(request.tags()));

        LinkResponse linkResponse = grpcMapper.linkToGrpcLinkResponse(subscriptionService.trackLink(chatLink));

        responseObserver.onNext(linkResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteLink(RemoveLinkRequest grpcreq, StreamObserver<LinkResponse> responseObserver) {
        backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest request =
                grpcMapper.mapRemoveLinkRequest(grpcreq);

        Chat chat = dtoEntityMapper.getChatFromChatId(grpcreq.getChatId());
        Link link = dtoEntityMapper.linkFromRemoveLinkRequest(request);
        ChatLink chatLink = new ChatLink(link, chat);

        chatLink = subscriptionService.untrackLink(chatLink);

        LinkResponse linkResponse = grpcMapper.linkToGrpcLinkResponse(chatLink);
        responseObserver.onNext(linkResponse);
        responseObserver.onCompleted();
    }
}
