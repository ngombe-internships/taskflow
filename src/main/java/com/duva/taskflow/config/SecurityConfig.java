package com.duva.taskflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;


@Configuration
@EnableMethodSecurity //  Permet d'utiliser @PreAuthorize sur les méthodes
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                //  Désactive CSRF (API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)

                //  Configuration des autorisations HTTP
                .authorizeHttpRequests(auth -> auth

                        //  Endpoint public : inscription
                        .requestMatchers("/api/auth/register").permitAll()

                        //  Routes réservées aux ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        //  Toutes les autres routes nécessitent authentification
                        .anyRequest().authenticated()
                )

                //  Désactive login HTML par défaut
                .formLogin(AbstractHttpConfigurer::disable)

                //  Désactive HTTP Basic (on utilisera JWT plus tard)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    //  Bean utilisé pour hasher les mots de passe avec BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
