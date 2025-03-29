package com.flagcamp.TripPlanner.service;

import com.flagcamp.TripPlanner.entity.UserEntity;
import com.flagcamp.TripPlanner.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsManager userDetailsManager;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserDetailsManager userDetailsManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsManager = userDetailsManager;
    }

    @Transactional
    public void signUp(String email, String password, String firstName, String lastName) {
        email = email.toLowerCase();
        UserDetails user = User.builder()
                .username(email)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build();
        userDetailsManager.createUser(user);
        userRepository.updateNameByEmail(email, firstName, lastName);

        UserEntity savedUser = userRepository.findByEmail(email);

    }

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
