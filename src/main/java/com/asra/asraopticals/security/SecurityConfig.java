package com.asra.asraopticals.security;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	http
    	 // ── CSRF ───────────────────────────────────────────────────────
    	.csrf(csrf -> csrf
    		    .ignoringRequestMatchers("/h2-console/**", "/coupon/validate","/cart/add/**",    // ← add this
    		            "/cart/remove/**",
    		            "/wishlist/add/**")
    		    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    		    .csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler())
    		)  
        // ── HEADERS ────────────────────────────────────────────────────
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(ct -> {})       	
                .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
            )
         // Public routes
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login", "/register", "/verify",
                    "/forgot-password", "/reset-password",
                    "/product/**", "/shop", "/shop/**", "/offers",
                    "/wishlist", "/wishlist/**",
                    "/cart", "/cart/**",
                    "/track-order", "/my-orders",
                    "/book-appointment", "/coupon/validate",
                    "/uploads/**",
                    "/css/**", "/js/**", "/images/**", "/static/**",
                    "/h2-console/**", "/oauth2/**", "/error",
                    "/search-suggest"
                ).permitAll()
             // Admin only
                .requestMatchers("/admin/**").hasRole("ADMIN")
             // Everything else requires login (checkout, wishlist, profile, reviews)
                .anyRequest().authenticated()
                
            )
            
            // ── FORM LOGIN ────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((req, res, auth) -> {
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    res.sendRedirect(isAdmin ? "/admin/dashboard" : "/");
                })
                .failureUrl("/login?error")
                .permitAll()
            )
            
         // ── GOOGLE OAUTH2 ─────────────────────────────────────────────
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
            )
            
            // ── LOGOUT ───────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
         // ── ACCESS DENIED HANDLER ─────────────────────────────────────
            // Redirect to login instead of crashing on missing 403.html
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/login")
            )
            .sessionManagement(session -> session
                .maximumSessions(3)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }
}