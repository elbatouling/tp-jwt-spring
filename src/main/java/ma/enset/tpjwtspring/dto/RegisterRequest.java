package ma.enset.tpjwtspring.dto;
//une record ne doit pas etre modifier
// c juste une variable pour stocker une ensemble de donnees
//Un record génère déjà les getters (email(), password()), le constructeur, equals, hashCode et toString.
public record RegisterRequest(
        String email,
        String password
) {
}