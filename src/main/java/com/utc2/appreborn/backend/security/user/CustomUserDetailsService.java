package com.utc2.appreborn.backend.security.user;

import com.utc2.appreborn.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
// tạo constructor tự động cho final field
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    // load user từ database khi login
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username)

                // chuyển User thành UserDetails
                .map(CustomUserDetails::new)

                // báo lỗi nếu không tìm thấy user
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}