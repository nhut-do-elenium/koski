package fi.opetushallitus.koski.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Objects;

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
}
