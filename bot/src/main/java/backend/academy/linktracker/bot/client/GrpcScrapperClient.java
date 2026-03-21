package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.client.responsehandler.GrpcScrapperBadResponseHandler;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.utils.GrpcMapper;
import backend.academy.linktracker.proto.AddLinkRequest;
import backend.academy.linktracker.proto.GetLinksRequest;
import backend.academy.linktracker.proto.RegisterChatRequest;
import backend.academy.linktracker.proto.RemoveLinkRequest;
import backend.academy.linktracker.proto.ScrapperServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GrpcScrapperClient {

    private final ScrapperServiceGrpc.ScrapperServiceBlockingStub stub;
    private final GrpcScrapperBadResponseHandler responseHandler;
    private final GrpcMapper mapper;

    public void registerChat(Long chatId) {
        try {
            RegisterChatRequest request =
                    RegisterChatRequest.newBuilder().setChatId(chatId).build();

            stub.registerChat(request);
        } catch (StatusRuntimeException e) {
            throw responseHandler.handle(e);
        }
    }

    public LinkResponse addLink(Long chatId, AddLinkRequest request) {

        try {
            return mapper.mapLinkResponseFromProto(stub.addLink(request));
        } catch (StatusRuntimeException e) {
            throw responseHandler.handle(e);
        }
    }

    public ListLinksResponse getLinks(Long chatId) {
        GetLinksRequest request = GetLinksRequest.newBuilder().setChatId(chatId).build();

        try {
            return mapper.mapListLinksResponseFromProto(stub.getLinks(request));
        } catch (StatusRuntimeException e) {
            throw responseHandler.handle(e);
        }
    }

    public LinkResponse deleteLink(Long chatId, RemoveLinkRequest request) {

        return mapper.mapLinkResponseFromProto(stub.deleteLink(request));
    }
}
