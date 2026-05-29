package by.chaika19.authservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String TIME = "time";

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> errorBody = Map.of(
                STATUS, HttpServletResponse.SC_FORBIDDEN,
                MESSAGE, "Access denied: You do not have enough permissions to access this resource: " + accessDeniedException.getMessage(),
                TIME, Instant.now().toString()
        );

        objectMapper.writeValue(response.getOutputStream(), errorBody);
    }
}
