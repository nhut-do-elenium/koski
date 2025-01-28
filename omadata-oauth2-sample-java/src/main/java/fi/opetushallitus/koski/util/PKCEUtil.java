package fi.opetushallitus.koski.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PKCEUtil {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateRandomPKCECodeVerifier() {
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return base64UrlEncoder.encodeToString(codeVerifier);
    }

    public static String generateCodeChallenge(String codeVerifier) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] encodedHash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encodedHash);
    }

    public static void main(String[] args) {
        String codeVerifier = generateRandomPKCECodeVerifier();
        System.out.println("Random PKCE Code Verifier: " + codeVerifier);
    }
}
