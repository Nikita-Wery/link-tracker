package backend.academy.linktracker.scrapper.client.inner.impl;

import backend.academy.linktracker.proto.BotServiceGrpc;
import backend.academy.linktracker.scrapper.client.inner.BotClient;
import backend.academy.linktracker.scrapper.client.responsehandler.GrpcBotBadResponseHandler;
import backend.academy.linktracker.scrapper.domain.MessageStatus;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GrpcBotClient implements BotClient<LinkUpdate> {

    private final BotServiceGrpc.BotServiceBlockingStub stub;
    private final GrpcBotBadResponseHandler responseHandler;

    public CompletableFuture<MessageStatus> sendUpdate(LinkUpdate linkUpdateArg) {
        backend.academy.linktracker.proto.LinkUpdate linkUpdateProto =
                backend.academy.linktracker.proto.LinkUpdate.newBuilder()
                        .setId(linkUpdateArg.id())
                        .setUrl(linkUpdateArg.url().toString())
                        .setDescription(linkUpdateArg.description())
                        .addAllTgChatIds(linkUpdateArg.tgChatIds())
                        .build();

        try {

            stub.sendUpdate(linkUpdateProto);
            return CompletableFuture.completedFuture(MessageStatus.SENT);
        } catch (StatusRuntimeException e) {
            throw responseHandler.handle(e);
        }
    }
}
