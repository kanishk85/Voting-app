package com.voting.service;

import com.voting.entity.Role;
import com.voting.entity.User;
import com.voting.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String name, String email, String phone, String password) {
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("That email address already has an account.");
        }
        try {
            users.saveAndFlush(new User(name.trim(), normalizedEmail, passwordEncoder.encode(password), phone.trim(), Role.ROLE_NORMAL));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("That email address already has an account.");
        }
    }
}
