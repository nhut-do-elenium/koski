package fi.opetushallitus.koski.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean("koski")
    public RestClient oauthRestClient(SslBundles sslBundles) {
        return RestClient.builder()
                .requestFactory(
                        ClientHttpRequestFactoryBuilder.jdk()
                                .build(
                                        ClientHttpRequestFactorySettings.defaults()
                                                .withSslBundle(sslBundles.getBundle("koski-ssl-bundle"))
                                                .withConnectTimeout(Duration.ofSeconds(5))
                                                .withReadTimeout(Duration.ofSeconds(10))))
                .messageConverters(
                        messageConverters -> {
                            messageConverters.clear();
                            messageConverters.add(new FormHttpMessageConverter());
                            messageConverters.add(new OAuth2AccessTokenResponseHttpMessageConverter());
                        })
                .defaultStatusHandler(new OAuth2ErrorResponseErrorHandler())
                .build();
    }
}
