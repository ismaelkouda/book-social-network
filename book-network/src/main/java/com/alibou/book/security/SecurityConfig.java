package com.alibou.book.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    // Filtre personnalisé qui intercepte chaque requête pour vérifier la présence et la validité du JWT.
    // C'est comme un HttpInterceptor, sauf qu'il s'exécute sur le serveur.
    private final JwtFilter jwtAuthFilter;
    // Composant chargé de vérifier l'identité de l'utilisateur (email + mot de passe, JWT, etc.).
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // Désactive la protection CSRF.
                // Une API REST utilisant des JWT n'utilise pas de session HTTP,
                // cette protection n'est donc généralement pas nécessaire.
                .csrf(AbstractHttpConfigurer::disable)
                // Configuration des autorisations.
                .authorizeHttpRequests(req -> req
                        // Routes publiques : aucun JWT n'est nécessaire.
                        .requestMatchers("/auth/**", "/webjars/**",
                                "/v2/api-docs", "/v3/api-docs", "/v3/api-docs/**",
                                "/swagger-resources", "/swagger-resources/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/configuration/ui", "/configuration/security"
                        )
                        // Tout le monde peut accéder à ces routes.
                        .permitAll()
                        // Toutes les autres routes nécessitent un utilisateur authentifié.
                        .anyRequest().authenticated())
                // Aucune session HTTP n'est conservée.
                // Chaque requête doit contenir un JWT valide dans le header Authorization.
                // Pourquoi STATELESS ? Parce que le JWT contient déjà toutes les informations nécessaires, donc Spring n'a donc pas besoin de mémoriser l'utilisateur en mémoire.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Indique à Spring Security quel mécanisme utiliser pour vérifier l'identité d'un utilisateur.
                //
                // Ici on injecte le Bean AuthenticationProvider déclaré dans BeansConfig.
                //
                // Concrètement, lorsqu'on appelle :
                //
                // authenticationManager.authenticate(...)
                //
                // Spring délègue automatiquement la vérification
                // au DaoAuthenticationProvider.
                //
                // Celui-ci exécutera les étapes suivantes :
                //
                // 1. appeler UserDetailsService.loadUserByUsername(email) dans le fichier UserDetailsServiceImpl
                // 2. récupérer le User depuis PostgreSQL
                // 3. comparer le mot de passe avec PasswordEncoder
                // 4. construire un objet Authentication si tout est valide
                .authenticationProvider(authenticationProvider)
                // Indique à Spring d'exécuter notre JwtFilter avant le UsernamePasswordAuthenticationFilter.
                // Le JwtFilter sera exécuté pour CHAQUE requête HTTP.
                // A l'intérieur du filtre, nous décidons ensuite :
                // - si la route est publique (/auth/**), on laisse passer immédiatement.
                // - si la route est protégée, on vérifie la présence et la validité du JWT.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }
}
