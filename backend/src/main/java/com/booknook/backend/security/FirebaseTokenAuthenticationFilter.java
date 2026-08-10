package com.booknook.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Verifies the {@code Authorization: Bearer <Firebase ID token>} header on every request, then
 * checks the token's email against {@link AllowlistService}.
 *
 * <p>Three outcomes:
 * <ul>
 *   <li>No/invalid token: request continues unauthenticated — {@link SecurityConfig} rejects it
 *       with 401 for any endpoint that requires auth.</li>
 *   <li>Valid token, email not on the allowlist: this filter itself writes a 403. The user
 *       authenticated fine with Firebase, they're just not invited to this app.</li>
 *   <li>Valid token, allowlisted: {@link FirebaseAuthenticatedUser} is set as the principal and
 *       the request proceeds.</li>
 * </ul>
 */
public class FirebaseTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenAuthenticationFilter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AllowlistService allowlistService;

    public FirebaseTokenAuthenticationFilter(AllowlistService allowlistService) {
        this.allowlistService = allowlistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = header.substring("Bearer ".length());
        FirebaseToken decoded;
        try {
            // Resolved per-request rather than injected: FirebaseAuth is a final class Spring
            // can't lazily proxy, and this filter must not force Firebase init at app startup.
            decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            log.debug("Rejected invalid Firebase ID token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        } catch (IllegalStateException e) {
            // FirebaseApp isn't initialized (FIREBASE_CREDENTIALS_PATH not configured yet) —
            // treat as unauthenticated rather than a 500.
            log.error("Firebase Admin SDK is not initialized: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        String email = decoded.getEmail();
        if (!allowlistService.isAllowed(email)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                    "not_invited", "This account is not on the Booknook invite list.");
            return;
        }

        FirebaseAuthenticatedUser user = new FirebaseAuthenticatedUser(decoded.getUid(), email, decoded.getName());
        var authentication = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void writeJson(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Map.of("error", error, "message", message)));
    }
}
