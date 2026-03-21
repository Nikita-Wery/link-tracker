package backend.academy.linktracker.bot.controller.grpc;

import backend.academy.linktracker.bot.service.LinkUpdateService;
import backend.academy.linktracker.bot.utils.GrpcMapper;
import backend.academy.linktracker.proto.BotServiceGrpc;
import backend.academy.linktracker.proto.LinkUpdate;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GrpcUpdateEndpoint extends BotServiceGrpc.BotServiceImplBase {

    private final LinkUpdateService linkUpdateService;
    private final GrpcMapper mapper;

    @Override
    public void sendUpdate(LinkUpdate request, StreamObserver<Empty> responseObserver) {
        linkUpdateService.sendUpdateMessage(mapper.mapLinkUpdateFromProto(request));
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
