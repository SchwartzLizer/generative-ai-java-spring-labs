package com.schwartzlizer.support.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import java.util.UUID;

@Configuration
public class SecurityConfiguration {
    @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService(SecurityProperties properties, PasswordEncoder encoder) {
        String agentPassword = properties.agentPassword().isBlank() ? UUID.randomUUID().toString() : properties.agentPassword();
        String adminPassword = properties.adminPassword().isBlank() ? UUID.randomUUID().toString() : properties.adminPassword();
        return new InMemoryUserDetailsManager(
            User.withUsername(properties.agentUsername()).password(encoder.encode(agentPassword)).roles("AGENT").build(),
            User.withUsername(properties.adminUsername()).password(encoder.encode(adminPassword)).roles("AGENT", "ADMIN").build());
    }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**", "/login", "/css/**", "/error").permitAll()
            .requestMatchers("/actuator/info").hasRole("ADMIN")
            .requestMatchers("/actuator/**").hasRole("ADMIN")
            .requestMatchers("/api/**").hasAnyRole("AGENT", "ADMIN")
            .requestMatchers("/", "/dashboard", "/feedback/**").hasAnyRole("AGENT", "ADMIN")
            .anyRequest().permitAll())
            .httpBasic(basic -> {})
            .formLogin(form -> form.defaultSuccessUrl("/dashboard", false))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        return http.build();
    }
}
