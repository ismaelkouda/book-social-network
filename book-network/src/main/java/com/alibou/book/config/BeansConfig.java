package com.alibou.book.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class BeansConfig {
    // Service chargé de récupérer un utilisateur depuis la base.
    // DaoAuthenticationProvider l'utilisera automatiquement.
    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // DaoAuthenticationProvider est l'implémentation fournie
        // par Spring Security pour authentifier un utilisateur
        // stocké dans une base de données.
        // Il délègue la recherche de l'utilisateur au UserDetailsService => UserDetailsServiceImpl
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        // Indique comment vérifier les mots de passe. quel methode utiliser pour la verification
        // ici passwordEncoder() qui appel new BCryptPasswordEncoder()
        // plutôt que de comparer les chaînes de caractères.
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        // Bean enregistré dans le conteneur Spring.
        // C'est ce Bean qui sera injecté dans SecurityConfig
        // (c'est ce concepte de verification qui est injecté dans le fichier class SecurityConfig via :
        // private final AuthenticationProvider authenticationProvider;).
        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt est recommandé pour stocker les mots de passe.
        // Il chiffre le mot de passe avant son enregistrement
        // et permet ensuite de vérifier un mot de passe
        // sans jamais connaître sa valeur originale.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuditorAware<Integer> auditorAware() {
        return new ApplicationAuditAware();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws AuthenticationException {
        return config.getAuthenticationManager();
    }

}
