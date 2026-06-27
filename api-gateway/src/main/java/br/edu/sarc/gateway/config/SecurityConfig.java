package br.edu.sarc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origin:http://localhost:3000}")
    private String corsAllowedOrigin;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/api/public/**",
                                "/api/schedules",
                                "/api/schedules/**",
                                "/api/resources/public",
                                "/api/resources/public/**",
                                "/api/allocations/public",
                                "/api/allocations/public/**",
                                "/api/users/professors",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/allocations/my").hasAnyRole("PROFESSOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/allocations/**").hasAnyRole("PROFESSOR", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/allocations/**").hasAnyRole("PROFESSOR", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/allocations/**").hasAnyRole("PROFESSOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/resources/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/resources/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/resources/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/resources/**").hasRole("ADMIN")
                        .pathMatchers("/api/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/resources/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/**").authenticated()
                        .pathMatchers(HttpMethod.PATCH, "/api/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(corsAllowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> Mono.just(new JwtAuthenticationToken(jwt, extractAuthorities(jwt)));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Object realmAccessClaim = jwt.getClaims().get("realm_access");

        if (realmAccessClaim instanceof Map<?, ?> realmAccess) {
            Object rolesClaim = realmAccess.get("roles");
            if (rolesClaim instanceof Collection<?> roles) {
                roles.stream()
                        .map(String::valueOf)
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
        }

        return authorities;
    }
}
