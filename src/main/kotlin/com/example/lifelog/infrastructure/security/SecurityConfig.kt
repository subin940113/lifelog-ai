package com.example.lifelog.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security 설정
 */
@Configuration
class SecurityConfig(
    private val jwtProvider: JwtProvider,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/health").permitAll()
                it.requestMatchers("/api/auth/**").permitAll()
                it.anyRequest().authenticated()
            }.addFilterBefore(JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
