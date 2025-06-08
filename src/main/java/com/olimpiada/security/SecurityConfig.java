package com.olimpiada.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/start", "/olympiads", "/olympiads/**", "/api/auth/register", "/css/**", "/js/**", "/images/**", "/olympiad_uploads/**").permitAll()
                .requestMatchers("/api/results/olympiad/**").permitAll()
                .requestMatchers("/api/messages", "/api/messages/").permitAll()
                .requestMatchers("/api/messages/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/profile").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/user/olympiad/**").hasRole("USER")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/feedback").permitAll()
                .requestMatchers("/forgot-password", "/reset-password").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new com.olimpiada.security.CustomAuthenticationSuccessHandler();
    }
} 