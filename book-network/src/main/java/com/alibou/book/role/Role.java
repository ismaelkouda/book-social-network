package com.alibou.book.role;

import com.alibou.book.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_role")
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String name;

    // Côté inverse de la relation Many-to-Many.
    //
    // mappedBy = "roles" indique que la relation est
    // gérée par l'attribut "roles" de l'entité User.
    // Cette entité ne crée donc pas une seconde table de liaison.
    @ManyToMany(mappedBy = "roles")
    // Ignore cette propriété lors de la conversion en JSON.
    // Évite une boucle infinie :
    // User -> Role -> User -> Role -> ...
    @JsonIgnore
    private List<User> users;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}
