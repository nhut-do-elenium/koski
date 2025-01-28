package fi.opetushallitus.koski.config;

import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

@Configuration(proxyBeanMethods = false)
public class WebClientConfig {

    private static final String SSL_BUNDLE_KOSKI = "koski-ssl-bundle";

    @Bean("koski-client")
    public WebClient clientWebClient(OAuth2AuthorizedClientManager authorizedClientManager,
                                     SslBundles sslBundles) throws Exception {
        var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        return WebClient.builder()
                .clientConnector(createClientConnector(sslBundles.getBundle(SSL_BUNDLE_KOSKI)))
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

    private static ClientHttpConnector createClientConnector(SslBundle sslBundle) throws Exception {
        var keyManagerFactory = sslBundle.getManagers().getKeyManagerFactory();
        var trustManagerFactory = sslBundle.getManagers().getTrustManagerFactory();

        var sslContext = SslContextBuilder.forClient()
                .keyManager(keyManagerFactory)
                .trustManager(trustManagerFactory)
                .build();

        var sslProvider = SslProvider.builder().sslContext(sslContext).build();
        var httpClient = HttpClient.create().secure(sslProvider);
        return new ReactorClientHttpConnector(httpClient);
    }
}
