package by.chaika19.authservice.model;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ROLE_USER,
    ROLE_ADMIN;

    @Override
    public @NonNull String getAuthority() {
        return this.name();
    }
}
