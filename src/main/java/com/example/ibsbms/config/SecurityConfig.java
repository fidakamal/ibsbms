package com.example.ibsbms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder) {

        UserDetails maker =
                org.springframework.security.core.userdetails.User
                        .withUsername("test.maker")
                        .password(
                                passwordEncoder.encode("maker123")
                        )
                        .roles("MAKER")
                        .build();

        UserDetails checker =
                org.springframework.security.core.userdetails.User
                        .withUsername("checker")
                        .password(
                                passwordEncoder.encode("checker123")
                        )
                        .roles("CHECKER")
                        .build();

        return new InMemoryUserDetailsManager(
                maker,
                checker
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Public pages/resources
                         */
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/static.css/**"
                        ).permitAll()

                        /*
                         * Maker-only
                         */
                        .requestMatchers(
                                "/shareholders/create"
                        ).hasRole("MAKER")

                        .requestMatchers(
                                "/shareholders/*/edit"
                        ).hasRole("MAKER")

                        .requestMatchers(
                                "/shareholders/returned",
                                "/shareholders/returned/**"
                        ).hasRole("MAKER")

                        .requestMatchers(
                                "/my-requests",
                                "/my-requests/**"
                        ).hasRole("MAKER")

                        /*
                         * Checker-only
                         */
                        .requestMatchers(
                                "/approvals",
                                "/approvals/**"
                        ).hasRole("CHECKER")

                        /*
                         * Maker + Checker
                         */
                        .requestMatchers(
                                "/shareholders",
                                "/shareholders/**"
                        ).hasAnyRole(
                                "MAKER",
                                "CHECKER"
                        )

                        .requestMatchers(
                                "/"
                        ).hasAnyRole(
                                "MAKER",
                                "CHECKER"
                        )

                        /*
                         * Everything else requires login
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * Login
                 */
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                /*
                 * Logout
                 */
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}