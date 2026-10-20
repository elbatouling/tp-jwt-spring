package ma.enset.tpjwtspring.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    // GET /api/hello : accessible uniquement avec un JWT valide
    @GetMapping("/hello")
    public String hello() {
        return "Bienvenue dans l'API sécurisée";
    }

    // GET /api/profile : retourne l'email de l'utilisateur authentifié
    @GetMapping("/profile")
    public String profile(Authentication authentication) {
        return "Utilisateur connecté : " + authentication.getName();
    }
}