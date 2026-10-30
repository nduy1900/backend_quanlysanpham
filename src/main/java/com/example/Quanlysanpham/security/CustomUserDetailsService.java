package com.example.Quanlysanpham.security;

import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        // Lấy danh sách quyền từ bảng UserRole
        var authorities = user.getUserRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority(
                        "ROLE_" + userRole.getRole().getName()
                ))
                .collect(Collectors.toList());

        // Chuyển Entity sang UserDetails
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(authorities)
                .build();
    }
}
