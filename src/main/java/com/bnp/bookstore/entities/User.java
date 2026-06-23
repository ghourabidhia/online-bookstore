package com.bnp.bookstore.entities;


import jakarta.persistence.*;

import lombok.*;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.List;


/**
 * A user account stored in the database.
 * Implements UserDetails so Spring Security can use it for login checks.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {


    @Id
    @GeneratedValue(strategy =
            GenerationType.IDENTITY)
    private Long id;


    private String firstname;


    private String lastname;


    @Column(
            unique = true,
            nullable = false
    )
    private String email;


    private String password;


    @Enumerated(EnumType.STRING)
    private Role role;


    /** Returns the user's role as a permission, for example ROLE_USER. */
    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + role.name()
                )
        );

    }


    /** Returns the email address as the login name. Spring Security calls this. */
    @Override
    public @NonNull String getUsername() {

        return email;

    }


}