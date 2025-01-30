package fi.opetushallitus.koski.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    private final String SSL_BUNDLE_KOSKI = "koski-ssl-bundle";

    @Bean("koski")
    public RestClient oauthRestClient(RestClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager,
                                      SslBundles sslBundles) {
        return builder
                .requestInterceptor(new OAuth2ClientHttpRequestInterceptor(authorizedClientManager))
                .requestFactory(
                        ClientHttpRequestFactoryBuilder.jdk()
                                .build(
                                        ClientHttpRequestFactorySettings.defaults()
                                                .withSslBundle(sslBundles.getBundle(SSL_BUNDLE_KOSKI))
                                                .withConnectTimeout(Duration.ofSeconds(5))
                                                .withReadTimeout(Duration.ofSeconds(10))))
                .messageConverters(
                        messageConverters -> {
                            messageConverters.clear();
                            messageConverters.add(new FormHttpMessageConverter());
                            messageConverters.add(new OAuth2AccessTokenResponseHttpMessageConverter());
                            messageConverters.add(new StringHttpMessageConverter());
                            messageConverters.add(new MappingJackson2HttpMessageConverter());
                        })
                .defaultStatusHandler(new OAuth2ErrorResponseErrorHandler())
                .build();
    }
}
