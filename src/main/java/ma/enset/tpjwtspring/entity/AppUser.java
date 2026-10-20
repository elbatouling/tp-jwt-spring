// Package où se trouve la classe (couche "entity" = modèles liés à la base de données)
package ma.enset.tpjwtspring.entity;

// Importe toutes les annotations JPA (Entity, Table, Id, Column, GeneratedValue...)
import jakarta.persistence.*;
// Annotations Lombok qui génèrent du code automatiquement à la compilation
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Indique à JPA/Hibernate que cette classe est une entité :
// elle sera mappée à une table de la base de données
@Entity

// Nom de la table en base : "users"
// (on évite "user" car c'est un mot réservé dans certains SGBD comme PostgreSQL)
@Table(name = "users")

// Lombok : génère automatiquement getters, setters, toString(),
// equals() et hashCode() pour tous les attributs
@Data

// Lombok : génère un constructeur sans arguments (obligatoire pour JPA)
@NoArgsConstructor

// Lombok : génère un constructeur avec tous les attributs (id, email, password, role)
@AllArgsConstructor
public class AppUser {

    // Clé primaire de la table
    @Id
    // La valeur de l'id est générée par la base (AUTO_INCREMENT)
    // IDENTITY = auto-incrément géré par le SGBD (MySQL, PostgreSQL...)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Colonne "email" avec contrainte d'unicité :
    // deux utilisateurs ne peuvent pas avoir le même email
    @Column(unique = true)
    private String email;

    // Mot de passe de l'utilisateur
    // (doit être stocké haché avec BCrypt, jamais en clair)
    private String password;

    // Rôle de l'utilisateur (ex : "ADMIN", "USER")
    // utilisé par Spring Security pour l'autorisation
    private String role;
}