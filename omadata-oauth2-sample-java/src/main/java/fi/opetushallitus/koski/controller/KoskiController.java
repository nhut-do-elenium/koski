package fi.opetushallitus.koski.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController("/")
public class KoskiController {

    private final RestClient restClient;

    @Value("${koski.resource-server.url}")
    private String resourceServerUrl;

    public KoskiController(RestClient restClient) {
        this.restClient = restClient;
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
    public ResponseEntity<String> authenticatedPost(@RequestBody(required = false) String body) {
        // Forward the POST request to the target URL
        return restClient.post()
                .uri("http://localhost:7051/login/oauth2/callback/koski")
                .contentType(MediaType.TEXT_PLAIN) // Use TEXT_PLAIN for raw string
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/error")
    public String error() {
        return "Error";
    }
}
