package fi.opetushallitus.koski.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                .oauth2Client(withDefaults()) //Trigger login with /oauth2/authorization/koski
                .build();
    }
}
