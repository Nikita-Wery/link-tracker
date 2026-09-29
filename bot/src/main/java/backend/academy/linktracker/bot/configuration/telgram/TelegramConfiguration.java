package backend.academy.linktracker.bot.configuration.telgram;

import backend.academy.linktracker.bot.properties.ProxyProperties;
import backend.academy.linktracker.bot.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import java.net.InetSocketAddress;
import java.net.Proxy;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramConfiguration {

    @Bean
    public TelegramBot telegramBot(TelegramProperties properties, ProxyProperties proxyProperties) {

//        Proxy proxy =
//                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort()));
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .proxy(proxy)
//                .proxyAuthenticator((route, response) -> {
//                    String credential = Credentials.basic(proxyProperties.getUserName(), proxyProperties.getPass());
//
//                    return response.request()
//                            .newBuilder()
//                            .header("Proxy-Authorization", credential)
//                            .build();
//                })
//                .build();

        Proxy proxy = new Proxy(
                Proxy.Type.SOCKS,
                new InetSocketAddress(
                        proxyProperties.getHost(),
                        proxyProperties.getPort()
                )
        );

        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .build();

        var builder = new TelegramBot.Builder(properties.getToken())
                .okHttpClient(client)
                .apiUrl(properties.getUrl())
                .updateListenerSleep(properties.getUpdateListenerSleep().toMillis());

        if (properties.isDebug()) {
            builder.debug();
        }

        return builder.build();
    }
}
