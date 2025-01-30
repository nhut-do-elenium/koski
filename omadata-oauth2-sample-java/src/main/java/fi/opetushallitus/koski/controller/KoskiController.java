package fi.opetushallitus.koski.controller;

import fi.opetushallitus.koski.config.KoskiConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@RestController("/")
public class KoskiController {

    private final RestClient restClient;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    private final KoskiConfig koskiConfig;

    public KoskiController(@Qualifier(KoskiConfig.RESTCLIENT_ID) RestClient restClient,
                           OAuth2AuthorizedClientRepository authorizedClientRepository,
                           KoskiConfig koskiConfig) {
        this.restClient = restClient;
        this.authorizedClientRepository = authorizedClientRepository;
        this.koskiConfig = koskiConfig;
    }

    @GetMapping("/")
    public String home(Authentication authentication, HttpServletRequest request) {
        var authorizedClient = getKoskiAuthorizedClient(authentication, request);
        if (authorizedClient == null) {
            return getUserNotAuthenticatedHtml();
        }
        return getUserAuthenticatedHtml(authorizedClient.getAccessToken());
    }

    private String getUserNotAuthenticatedHtml() {
        return """
                <center>
                You are NOT authenticated.<br/>
                <br/><h1><a href="/oauth2/authorization/%s">Login</a></h1>
                </center>
                """.formatted(koskiConfig.getRegistrationId());
    }

    private String getUserAuthenticatedHtml(OAuth2AccessToken accessToken) {
        return """
                <center>
                You are authenticated.<br/>
                <br/>Access token: %s
                <br/>Issued at: %s
                <br/>Expires at: %s
                <br/>Scopes: %s
                <br/><br/>
                <h2><a href="/resource-server/fetch">Fetch data from resource server</a></h2>
                <br/><h1><a href="/oauth2/logout/%s">Logout</a></h1>
                </center>
                """.formatted(accessToken.getTokenValue(),
                accessToken.getIssuedAt(), accessToken.getExpiresAt(), accessToken.getScopes(),
                koskiConfig.getRegistrationId());
    }

    private OAuth2AuthorizedClient getKoskiAuthorizedClient(Authentication authentication, HttpServletRequest request) {
        return authorizedClientRepository.loadAuthorizedClient(koskiConfig.getRegistrationId(), authentication, request);
    }

    @GetMapping("/oauth2/logout/koski")
    public String logoutGet(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        authorizedClientRepository.removeAuthorizedClient(koskiConfig.getRegistrationId(), authentication, request, response);
        return """
                <center>
                <h1>Logged out</h1>
                <br/>
                <h2><a href="/">Home</a></h2>
                </center>
                """;
    }

    @GetMapping("/resource-server/fetch")
    public String fetchData(Authentication authentication, HttpServletRequest request) {
        var koski = getKoskiAuthorizedClient(authentication, request);
        if (koski == null) {
            return getUserNotAuthenticatedHtml();
        }

        try {
            var accessToken = koski.getAccessToken().getTokenValue();
            return restClient.post()
                    .uri(koskiConfig.getResourceServer())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .attributes(clientRegistrationId(koskiConfig.getRegistrationId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            log.error("Failed to fetch data from resource server", e);
            return """
                    <center>
                    <h1>Failed to fetch data from resource server</h1>
                    <br/>
                    %s
                    <br/>
                    <h2><a href="/">Home</a></h2>
                    </center>
                    """.formatted(e.getMessage());
        }
    }
}
