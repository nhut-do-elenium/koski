package fi.opetushallitus.koski.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Objects;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@RestController("/")
public class KoskiController {

    private final RestClient restClient;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Value("${koski.resource-server.url}")
    private String resourceServerUrl;

    public KoskiController(@Qualifier("koski") RestClient restClient, OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.restClient = restClient;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @GetMapping("/")
    public String root(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        var koski = getKoskiAuthorizedClient(authentication, request);

        var sb = new StringBuilder("<center>");
        if (koski == null) {
            sb.append(
                    """
                            You are NOT authenticated.<br/>
                            <br/><h1><a href="/oauth2/authorization/koski">Login</a></h1>
                            """
            );

        } else {
            var accessToken = koski.getAccessToken();
            sb.append("""
                            You are authenticated.<br/>
                            <br/>Access token: %s
                            <br/>Issued at: %s
                            <br/>Expires at: %s
                            <br/>Scopes: %s
                            <br/><br/>
                            <h2><a href="/resource-server/fetch">Fetch data from resource server</a></h2>
                            <br/><h1><a href="/oauth2/logout/koski">Logout</a></h1>
                            """.formatted(accessToken.getTokenValue(),
                    accessToken.getIssuedAt(), accessToken.getExpiresAt(), accessToken.getScopes())
            );
        }
        return sb.append("</center>").toString();
    }

    private OAuth2AuthorizedClient getKoskiAuthorizedClient(Authentication authentication, HttpServletRequest request) {
        return authorizedClientRepository.loadAuthorizedClient("koski", authentication, request);
    }

    @GetMapping("/oauth2/logout/koski")
    public String logoutGet(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        authorizedClientRepository.removeAuthorizedClient("koski", authentication, request, response);
        return """
                <center><h1>Logged out</h1><br/><h2><a href="/">Home</a></h2></center>
                """;
    }

    @GetMapping("/resource-server/fetch")
    public String fetchData(Authentication authentication, HttpServletRequest request) {
        var koski = getKoskiAuthorizedClient(authentication, request);
        if (koski == null) {
            return """
                    <center><h1>You are not authenticated</h1><br/><h2><a href="/">Home</a></h2></center>
                    """;
        }

        try {
            return restClient.post()
                    .uri(resourceServerUrl)
                    .header("Authorization", "Bearer " + Objects.requireNonNull(koski.getAccessToken()).getTokenValue())
                    .attributes(clientRegistrationId("koski"))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            log.error("Failed to fetch data from resource server", e);
            return """
                    <center><h1>Failed to fetch data from resource server</h1><br/><h2><a href="/">Home</a></h2></center>
                    """;
        }
    }
}
