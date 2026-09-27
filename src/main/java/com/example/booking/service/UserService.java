package com.example.booking.service;

import com.example.booking.model.User;

public interface UserService {
    User findByUsername(String username);
    User findByEmail(String email);
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}