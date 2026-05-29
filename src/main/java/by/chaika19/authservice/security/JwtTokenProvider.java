package by.chaika19.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "ACCESS";
    public static final String TYPE_REFRESH = "REFRESH";

    private final SecretKey secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {

        if (secretKey == null || !secretKey.matches("^[0-9a-fA-F]{64,}$")) {
            log.error("CRITICAL: The 'jwt.secret' configuration property must be a valid, hex-encoded string containing at least 64 characters.");
            throw new IllegalStateException("Invalid jwt.secret configuration. Must be a valid Hex string of at least 64 characters.");
        }

        byte[] keyBytes = HexFormat.of().parseHex(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(Long userId, String role) {
        return generateToken(userId, role, TYPE_ACCESS, accessExpirationMs);
    }

    public String generateRefreshToken(Long userId, String role) throws NullPointerException {
        return generateToken(userId, role, TYPE_REFRESH, refreshExpirationMs);
    }

    private String generateToken(Long id, String role, String tokenType, long expirationMs) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(id.toString())
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public Optional<Claims> parseAndValidateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Invalid JWT signature (possible hacking attempt): {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token format: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
