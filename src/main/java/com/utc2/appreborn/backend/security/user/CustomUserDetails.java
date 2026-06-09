package com.utc2.appreborn.backend.security.user;

import com.utc2.appreborn.backend.modules.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRole() == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }
        // ADMIN và ADVISOR: 1 authority duy nhất
        if (!user.getRole().name().equals("STAFF")) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }
        // STAFF: trả authority tổng (ROLE_STAFF) + authority theo level (ROLE_STAFF_LEVEL_X)
        // Để các @PreAuthorize có thể check cả hasRole('STAFF') lẫn hasRole('STAFF_LEVEL_2')
        Integer lv = user.getStaffLevel();
        if (lv == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_STAFF"));
        }
        return List.of(
            new SimpleGrantedAuthority("ROLE_STAFF"),
            new SimpleGrantedAuthority("ROLE_STAFF_LEVEL_" + lv)
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /** Expose User entity để controller lấy userId, role, v.v. */
    public User getUser() {
        return user;
    }
}