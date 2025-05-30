package com.exchange.marketexchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user1 = User.builder()
                .username("user1")
                .password(passwordEncoder.encode("pass1"))
                .roles("USER")
                .build();
        UserDetails user2 = User.builder()
                .username("user2")
                .password(passwordEncoder.encode("pass2"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user1, user2);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF, common for APIs / JWT
            .authorizeHttpRequests(authz -> authz
                .antMatchers("/ws/**").permitAll() // Allow WebSocket connections
                .antMatchers("/api/auth/**").permitAll() // For login/registration endpoints later
                .antMatchers("/public/**").permitAll() // Any other public resources
                .anyRequest().authenticated() // All other requests require authentication
            )
            .httpBasic(); // Start with HTTP Basic auth for simplicity, can add formLogin or JWT later
            // .formLogin(form -> form.loginPage("/login").permitAll()); // Example if we add a login page

        return http.build();
    }
}
