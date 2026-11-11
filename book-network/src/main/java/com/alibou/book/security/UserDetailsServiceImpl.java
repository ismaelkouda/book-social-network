package com.alibou.book.security;

import com.alibou.book.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    // Repository chargé de communiquer avec PostgreSQL.
    private final UserRepository userRepository;

    @Override
    @Transactional
    @NullMarked
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Cette méthode n'est presque jamais appelée directement
        // par ton code.
        //
        // C'est DaoAuthenticationProvider qui l'appelle
        // automatiquement pendant la connexion.
        //
        // Flow :
        //
        // AuthenticationManager
        //          │
        //          ▼
        // DaoAuthenticationProvider
        //          │
        //          ▼
        // loadUserByUsername(email)
        //
        // Son rôle est uniquement de retrouver
        // l'utilisateur correspondant à l'email.
        //
        // Comme User implémente UserDetails,
        // il peut être retourné directement.
        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with username: " + username
                        )
                );
    }
}
