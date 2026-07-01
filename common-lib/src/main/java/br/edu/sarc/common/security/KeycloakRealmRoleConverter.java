package br.edu.sarc.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Converte um JWT emitido pelo Keycloak (realm "sarc") em uma Authentication do Spring Security,
 * extraindo as roles de {@code realm_access.roles} como {@link GrantedAuthority}. Reutilizado por
 * todos os resource servers do SARC para que a regra de mapeamento roles-do-token -> authorities
 * exista em um unico lugar.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt));
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
