package fi.opetushallitus.koski.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController("/")
public class KoskiController {

    private final WebClient webClient;

    @Value("${koski.resource-server.url}")
    private String resourceServerUrl;

    public KoskiController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/")
    public String root() {
        return """
                You are NOT authenticated.
                <br/><a href="/oauth2/authorization/koski">Login</a>
                """;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, You!";
    }

    @GetMapping("/secured")
    public String secured() {
        return "Secured";
    }

//    @GetMapping("/api/openid-api-test/form-post-response-cb")
//    public String authenticated(HttpServletRequest request) {
//        return "Login was successful.";
//    }

    @GetMapping("/api/openid-api-test/form-post-response-cb")
    public ResponseEntity<Void> authenticatedGet() {
        // Got the permissions.
        // Fetch data from Resource server.
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/openid-api-test/form-post-response-cb")
    public String authenticatedPost() {
        return "/";
    }

    @GetMapping("/error")
    public String error() {
        return "Error";
    }
}
