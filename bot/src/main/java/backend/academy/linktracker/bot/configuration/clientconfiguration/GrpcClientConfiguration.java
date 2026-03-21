package backend.academy.linktracker.bot.configuration.clientconfiguration;

import backend.academy.linktracker.bot.client.GrpcScrapperClient;
import backend.academy.linktracker.bot.client.responsehandler.GrpcScrapperBadResponseHandler;
import backend.academy.linktracker.bot.configuration.conditionals.GrpcApiConditional;
import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import backend.academy.linktracker.bot.utils.GrpcMapper;
import backend.academy.linktracker.proto.ScrapperServiceGrpc;
import io.grpc.ManagedChannel;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
@Conditional(GrpcApiConditional.class)
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
