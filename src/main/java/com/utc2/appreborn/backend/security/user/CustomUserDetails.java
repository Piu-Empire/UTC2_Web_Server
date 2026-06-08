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
        // Đọc role thật từ User entity thay vì hardcode ROLE_STUDENT
        if (user.getRole() == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
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