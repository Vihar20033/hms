package com.hms.user.service;

import com.hms.user.entity.User;
import com.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username)
            throws UsernameNotFoundException {

        return repository.findByUsername(username)
                .or(() -> repository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}