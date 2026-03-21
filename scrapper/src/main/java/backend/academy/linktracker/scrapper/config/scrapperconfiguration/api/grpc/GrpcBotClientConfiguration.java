package backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.grpc;

import backend.academy.linktracker.proto.BotServiceGrpc;
import backend.academy.linktracker.scrapper.client.inner.GrpcBotClient;
import backend.academy.linktracker.scrapper.client.responsehandler.GrpcBotBadResponseHandler;
import backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.conditional.GrpcApiConditional;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import io.grpc.ManagedChannel;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
@Conditional(GrpcApiConditional.class)
public class GrpcBotClientConfiguration {

    @Bean
    ManagedChannel stub(BotProperties properties, GrpcChannelFactory channels) {
        return channels.createChannel(properties.getGrpcHost());
    }

    @Bean
    public GrpcBotBadResponseHandler grpcBotBadResponseHandler(List<BotApiException> exceptionList) {
        return new GrpcBotBadResponseHandler(exceptionList);
    }

    @Bean
    public GrpcBotClient grpcBotClient(ManagedChannel channel, GrpcBotBadResponseHandler responseHandler) {
        return new GrpcBotClient(BotServiceGrpc.newBlockingStub(channel), responseHandler);
    }
}
