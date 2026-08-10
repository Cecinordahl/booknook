package com.booknook.backend.security;

import com.booknook.backend.repository.AllowlistRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AllowlistServiceTest {

    @Test
    void allowsWhitelistedEmailRegardlessOfCase() throws Exception {
        AllowlistRepository repository = mock(AllowlistRepository.class);
        when(repository.exists(eq("friend@example.com"))).thenReturn(true);

        AllowlistService service = new AllowlistService(repository);

        assertTrue(service.isAllowed("Friend@Example.com"));
    }

    @Test
    void rejectsEmailNotOnAllowlist() throws Exception {
        AllowlistRepository repository = mock(AllowlistRepository.class);
        when(repository.exists(eq("stranger@example.com"))).thenReturn(false);

        AllowlistService service = new AllowlistService(repository);

        assertFalse(service.isAllowed("stranger@example.com"));
    }

    @Test
    void rejectsNullOrBlankEmailWithoutQueryingFirestore() {
        AllowlistRepository repository = mock(AllowlistRepository.class);
        AllowlistService service = new AllowlistService(repository);

        assertFalse(service.isAllowed(null));
        assertFalse(service.isAllowed("  "));
    }
}
