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
        // STAFF level 5 → authority riêng để phân quyền KTX + học phần
        if (user.getRole().name().equals("STAFF") && Integer.valueOf(5).equals(user.getStaffLevel())) {
            return List.of(new SimpleGrantedAuthority("ROLE_STAFF_LEVEL_5"));
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