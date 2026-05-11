package com.utc2.appreborn.backend.security.user;

import com.utc2.appreborn.backend.modules.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// tạo constructor cho object user
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    // cấp quyền cho tài khoản đăng nhập
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(

                // chuyển role thành quyền spring security
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())

        );
    }

    @Override
    // trả về mật khẩu user hiện tại
    public String getPassword() {

        return user.getPassword();

    }

    @Override
    // trả về username user hiện tại
    public String getUsername() {

        return user.getUsername();

    }

    @Override
    // kiểm tra tài khoản còn hoạt động không
    public boolean isEnabled() {

        return user.isEnabled();

    }
}