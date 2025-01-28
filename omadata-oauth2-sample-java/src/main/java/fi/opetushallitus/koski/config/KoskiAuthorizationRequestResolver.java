package fi.opetushallitus.koski.config;

import fi.opetushallitus.koski.util.PKCEUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class KoskiAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String REGISTRATION_ID_KOSKI = "koski";

    public KoskiAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        var authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        var authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }

        var clientRegistration = clientRegistrationRepository.findByRegistrationId(REGISTRATION_ID_KOSKI);

        var additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
        additionalParameters.put("scope", String.join(" ", clientRegistration.getScopes()));
        additionalParameters.put("code_challenge", PKCEUtil.generateCodeChallenge(PKCEUtil.generateRandomPKCECodeVerifier()));
        additionalParameters.put("code_challenge_method", "S256");
        additionalParameters.put("response_mode", "form_post");
        additionalParameters.put("response_type", "code");

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}
