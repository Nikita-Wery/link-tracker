package backend.academy.linktracker.bot.configuration.clients;

import backend.academy.linktracker.bot.client.GrpcScrapperClient;
import backend.academy.linktracker.bot.client.responsehandler.GrpcScrapperBadResponseHandler;
import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import backend.academy.linktracker.bot.utils.mappers.GrpcMapper;
import backend.academy.linktracker.proto.ScrapperServiceGrpc;
import io.grpc.ManagedChannel;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
@ConditionalOnProperty(name = "app.client.scrapper.api.grpc.enabled", havingValue = "true")
public class GrpcClientConfiguration {

    @Bean
    public ManagedChannel stub(ScrapperProperties properties, GrpcChannelFactory channels) {
        return channels.createChannel(properties.getGrpcHost());
    }

    @Bean
    public GrpcScrapperBadResponseHandler grpcScrapperBadResponseHandler(List<ScrapperApiException> exceptionList) {
        return new GrpcScrapperBadResponseHandler(exceptionList);
    }

    @Bean
    public GrpcScrapperClient grpcScrapperClient(
            ManagedChannel channel, GrpcScrapperBadResponseHandler responseHandler, GrpcMapper mapper) {
        return new GrpcScrapperClient(ScrapperServiceGrpc.newBlockingStub(channel), responseHandler, mapper);
    }
}
