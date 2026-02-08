package com.learnsinhala.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.learnsinhala.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.learnsinhala.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Build authorities from user role
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Add role with ROLE_ prefix (Spring Security convention)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                authorities
        );
    }
}
