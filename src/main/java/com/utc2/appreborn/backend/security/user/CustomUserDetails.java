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
        // Tạm thời gán mặc định quyền ROLE_STUDENT để phục vụ việc test
        return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Dùng email làm username đăng nhập
    }

    @Override
    public boolean isEnabled() {
        return true; // Mặc định tài khoản luôn hoạt động
    }
}