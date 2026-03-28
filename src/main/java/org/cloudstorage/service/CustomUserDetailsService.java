package org.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.cloudstorage.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.getUserByUsername(username)
                .map(user -> new org.cloudstorage.model.security.UserDetails(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword()
                ))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
