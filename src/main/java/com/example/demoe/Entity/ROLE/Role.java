package com.example.demoe.Entity.ROLE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Role {
    USER(Collections.emptySet()),
//    SELLER(
//            Set.of(
//                    Permission.SELLER_CREATE,
//                    Permission.SELLER_DELETE,
//                    Permission.SELLER_UPDATE,
//                    Permission.SELLER_READ
//            )
//    ),
    ADMIN(
            Set.of(
                    Permission.ADMIN_READ,
                    Permission.ADMIN_UPDATE,
                    Permission.ADMIN_DELETE,
                    Permission.ADMIN_CREATE,
                    Permission.SELLER_CREATE,
                    Permission.SELLER_DELETE,
                    Permission.SELLER_UPDATE,
                    Permission.SELLER_READ
            )
    )

    ;

    @Getter
    private final Set<Permission> permissions;
    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }

}