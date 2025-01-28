package fi.opetushallitus.koski.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private final KoskiAuthorizationRequestResolver koskiAuthorizationRequestResolver;

    public SecurityConfig(KoskiAuthorizationRequestResolver koskiAuthorizationRequestResolver) {
        this.koskiAuthorizationRequestResolver = koskiAuthorizationRequestResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        "/",
                                        "/api/openid-api-test/form-post-response-cb"
                                )
                                .permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .authorizationEndpoint(authorizationEndpoint ->
                                        authorizationEndpoint
                                                .authorizationRequestResolver(koskiAuthorizationRequestResolver)
                                ).successHandler(new AuthenticationSuccessHandler() {
                                    @Override
                                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                                        //Why isn't this being called?
                                        System.out.println("Authentication success");
                                    }
                                }).failureHandler((request, response, exception) -> {
                                    response.sendRedirect("/error");
                                })
                )
                .oauth2Client(withDefaults())
                .formLogin(withDefaults()) //Remove when done.
                .build();
    }
}
