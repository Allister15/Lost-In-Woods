package com.DinoCorp.Lost_In_Woods.controller;

import com.DinoCorp.Lost_In_Woods.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final InMemoryUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    // Direct injection targeting our exact custom user manager bean name
    public AuthController(
            @Qualifier("myCustomGameUserManager") InMemoryUserDetailsManager userDetailsManager,
            PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {
        if (userDetailsManager.userExists(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        UserDetails newUser = User.withUsername(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles("USER")
                .build();

        userDetailsManager.createUser(newUser);
        return ResponseEntity.ok("Account created successfully!");
    }
}