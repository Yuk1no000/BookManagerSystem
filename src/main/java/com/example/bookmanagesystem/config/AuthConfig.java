package com.example.bookmanagesystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AuthConfig {

    private static final Logger log = LoggerFactory.getLogger(AuthConfig.class);

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService demoUsers(
            PasswordEncoder encoder,
            @Value("${DEMO_READER_PASSWORD}") String readerPassword,
            @Value("${DEMO_LIBRARIAN_PASSWORD}") String librarianPassword,
            @Value("${DEMO_ADMIN_PASSWORD}") String adminPassword
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername("reader")
                        .password(encoder.encode(readerPassword))
                        .roles("READER")
                        .build(),

                User.withUsername("librarian")
                        .password(encoder.encode(librarianPassword))
                        .roles("LIBRARIAN")
                        .build(),

                User.withUsername("admin")
                        .password(encoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/auth/login")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/auth/me")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/*")
                        .hasAnyRole("READER", "LIBRARIAN", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/books")
                        .hasAnyRole("LIBRARIAN", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/books/*")
                        .hasAnyRole("LIBRARIAN", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/books/*")
                        .hasRole("ADMIN")

                        .anyRequest().denyAll()
                )

                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) -> {
                            log.info("登录成功 user={} method={} path={}",
                                    authentication.getName(), request.getMethod(), request.getRequestURI());
                            response.setStatus(204);
                        })
                        .failureHandler((request, response, exception) ->{
                            log.warn("登录失败 method={} path={} reason={}",
                                    request.getMethod(), request.getRequestURI(),
                                    exception.getClass().getSimpleName());
                            response.setStatus(401);
                                })
                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            log.info("退出登录 user={} method={} path={}",
                                    authentication == null ? "anonymous" : authentication.getName(),
                                    request.getMethod(), request.getRequestURI());
                            response.setStatus(204);
                        })
                )

                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            log.warn("未登录访问 method={} path={} reason={}",
                                    request.getMethod(), request.getRequestURI(),
                                    exception.getClass().getSimpleName());
                            response.setStatus(401);
                        })
                        .accessDeniedHandler((request, response, exception) ->{
                            log.warn(
                                    "访问被拒绝,method={},url={},reason={}",
                                    request.getMethod(),
                                    request.getRequestURI(),
                                    exception.getClass().getSimpleName()
                            );
                            response.setStatus(403);
                        })
                )

                .build();
    }
}
