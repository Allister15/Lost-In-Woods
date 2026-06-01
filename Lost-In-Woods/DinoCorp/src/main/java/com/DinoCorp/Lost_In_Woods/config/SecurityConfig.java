package com.DinoCorp.Lost_In_Woods.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // This is critical for Spring to find your security config
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabling CSRF for game API testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all traffic for now to test connectivity
                );

        return http.build();
    }
}