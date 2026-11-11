package com.alibou.book.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${application.security.jwt.secret-key}")
    private String jwtSecret;

    // Extrait l'identifiant principal contenu dans le JWT. Lors de la génération du token, nous avons enregistré l'email dans le Subject.
    // Cette méthode permet donc de récupérer directement cet email.
    //
    // JWT
    // ↓
    // Subject
    // ↓
    // ismael@gmail.com
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Méthode générique permettant d'extraire n'importe quelle information du JWT.
    //
    // Exemple :
    //
    // extractClaim(token, Claims::getSubject)
    //
    // ou
    //
    // extractClaim(token, Claims::getExpiration)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Décode entièrement le JWT. Vérifie également que la signature correspond bien à notre clé secrète.
    // Si la signature est invalide, une exception sera levée.
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Point d'entrée principal pour créer un JWT.
    // Initialise une Map vide de claims puis délègue la construction du token.
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return buildToken(claims, userDetails, jwtExpiration);
    }

    // Construit le JWT.
    // On y ajoute notamment :
    // - le Subject (email)
    // - la date de création
    // - la date d'expiration
    // - les rôles (authorities)
    // Enfin le JWT est signé avec la clé secrète afin d'empêcher toute modification.

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long jwtExpiration) {
        var authorities = userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .claim("authorities", authorities)
                .signWith(getSignKey())
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);

    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Convertit la clé secrète stockée dans application.yml en objet SecretKey.
    // Cette clé servira à :
    // - signer les JWT
    // - vérifier leur signature.
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
