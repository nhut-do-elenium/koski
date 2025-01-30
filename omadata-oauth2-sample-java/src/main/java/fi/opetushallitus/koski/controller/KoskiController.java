package fi.opetushallitus.koski.controller;

import fi.opetushallitus.koski.config.KoskiConfig;
import jakarta.servlet.ServletException;
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

import java.io.IOException;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Slf4j
@RestController("/")
public class KoskiController {

    private static final String HOME_URL = "/";

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

    @GetMapping(HOME_URL)
    public String home(Authentication authentication, HttpServletRequest request) {
        var authorizedClient = getKoskiAuthorizedClient(authentication, request);
        if (authorizedClient == null) {
            return getUserNotAuthenticatedHtml();
        }
        var data = request.getServletContext().getAttribute("data");
        return getUserAuthenticatedHtml(authorizedClient.getAccessToken(), data == null ? null : data.toString());
    }

    private String getUserNotAuthenticatedHtml() {
        return """
                <center>
                You are NOT authenticated.<br/>
                <br/><h1><a href="/oauth2/authorization/%s">Login</a></h1>
                </center>
                """.formatted(koskiConfig.getRegistrationId());
    }

    private String getUserAuthenticatedHtml(OAuth2AccessToken accessToken, String fetchedData) {
        return """
                <center>
                <h1><a href="/oauth2/logout/%s">Logout</a></h1>
                <br/>You are authenticated.<br/>
                <br/>Access token: %s
                <br/>Issued at: %s
                <br/>Expires at: %s
                <br/>Scopes: %s
                <br/>
                <br/>Retrieved data from resource server:
                <br/><code>%s</code>
                </center>
                """.formatted(koskiConfig.getRegistrationId(),
                accessToken.getTokenValue(), accessToken.getIssuedAt(), accessToken.getExpiresAt(),
                accessToken.getScopes(), fetchedData == null ? "" : fetchedData);
    }

    private OAuth2AuthorizedClient getKoskiAuthorizedClient(Authentication authentication, HttpServletRequest request) {
        return authorizedClientRepository.loadAuthorizedClient(koskiConfig.getRegistrationId(), authentication, request);
    }

    @GetMapping("/oauth2/logout/koski")
    public void logoutGet(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        authorizedClientRepository.removeAuthorizedClient(koskiConfig.getRegistrationId(), authentication, request, response);
        response.sendRedirect(HOME_URL);
    }

    @GetMapping("/api/openid-api-test/form-post-response-cb")
    public void oAuth2DoneCallbackEndpoint(Authentication authentication,
                                           HttpServletRequest request, HttpServletResponse response
    ) throws IOException, ServletException {
        // The OAuth2 authorization code flow is completed (either fail or success).
        boolean validUser = true; // Check if the user is valid in the actual application.
        var auth2AuthorizedClient = getKoskiAuthorizedClient(authentication, request);
        if (auth2AuthorizedClient == null) {
            response.sendRedirect(HOME_URL);
            return;
        }
        var servletContext = request.getServletContext();
        servletContext.setAttribute("data", fetchDataFromResourceServer(auth2AuthorizedClient));
        var dispatcher = servletContext.getRequestDispatcher(HOME_URL);
        dispatcher.forward(request, response);
    }

    private String fetchDataFromResourceServer(OAuth2AuthorizedClient koski) {
        var accessToken = koski.getAccessToken().getTokenValue();
        return restClient.post()
                .uri(koskiConfig.getResourceServer())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .attributes(clientRegistrationId(koskiConfig.getRegistrationId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }
}
