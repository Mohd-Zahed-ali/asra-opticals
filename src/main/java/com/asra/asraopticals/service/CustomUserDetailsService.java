package com.asra.asraopticals.service;

import com.asra.asraopticals.model.User;
import com.asra.asraopticals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {

        // Try username → email → phone
        Optional<User> found = userRepository.findByUsername(input);
        if (found.isEmpty()) found = userRepository.findByEmail(input);
        if (found.isEmpty()) found = userRepository.findByPhone(input);
        if (found.isEmpty()) throw new UsernameNotFoundException("User not found: " + input);

        User u = found.get();

        // Block unverified accounts with a clear message
        if (!Boolean.TRUE.equals(u.getVerified())) {
            throw new DisabledException("Account not verified. Please check your email for OTP.");
        }

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(u.getRole()))
        );
    }
}