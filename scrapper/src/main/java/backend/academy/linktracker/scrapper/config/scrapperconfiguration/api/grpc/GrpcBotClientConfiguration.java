package backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.grpc;

import backend.academy.linktracker.proto.BotServiceGrpc;
import backend.academy.linktracker.scrapper.client.inner.impl.GrpcBotClient;
import backend.academy.linktracker.scrapper.client.responsehandler.GrpcBotBadResponseHandler;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import io.grpc.ManagedChannel;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
@ConditionalOnProperty(name = "app.client.bot.api.grpc.enabled", havingValue = "true")
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
