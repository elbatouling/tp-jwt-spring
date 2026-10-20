package ma.enset.tpjwtspring.dto;

public record LoginRequest(
        String email,
        String password
) {
}
