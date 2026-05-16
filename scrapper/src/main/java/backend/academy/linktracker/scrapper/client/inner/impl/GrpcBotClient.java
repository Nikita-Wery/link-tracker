package backend.academy.linktracker.scrapper.client.inner;

import backend.academy.linktracker.proto.BotServiceGrpc;
import backend.academy.linktracker.scrapper.client.responsehandler.GrpcBotBadResponseHandler;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GrpcBotClient {

    private final BotServiceGrpc.BotServiceBlockingStub stub;
    private final GrpcBotBadResponseHandler responseHandler;

    public void sendUpdate(LinkUpdate linkUpdateArg) {
        backend.academy.linktracker.proto.LinkUpdate linkUpdateProto =
                backend.academy.linktracker.proto.LinkUpdate.newBuilder()
                        .setId(linkUpdateArg.id())
                        .setUrl(linkUpdateArg.url().toString())
                        .setDescription(linkUpdateArg.description())
                        .addAllTgChatIds(linkUpdateArg.tgChatIds())
                        .build();

        try {
            stub.sendUpdate(linkUpdateProto);
        } catch (StatusRuntimeException e) {
            throw responseHandler.handle(e);
        }
    }
}
