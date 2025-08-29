//package org.plasma.common.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.Customizer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        logger.info("Configuring SecurityFilterChain");
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
//                        .requestMatchers("/actuator/**", "/metrics").hasRole("ADMIN")
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(Customizer.withDefaults())
//                .formLogin(form -> form.disable())
//                .csrf(csrf -> csrf.disable());
//        return http.build();
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//        logger.info("Configuring UserDetailsService with user 'admin'");
//        var user = User.withUsername("admin")
//                .password("{noop}secret")
//                .roles("ADMIN")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }
//}