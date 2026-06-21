package com.kavya.stealthpad.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfg {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http.csrf(csrfconfig -> csrfconfig.disable())
                            .sessionManagement(sessionConfig ->
                            sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                            .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**")
                            .permitAll()
                            .anyRequest().authenticated());                            
                            ;
        return http.build();
    }

    
}