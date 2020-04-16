package uibk.ac.at.prodigaclient.dtos;

public class JwtResponseDTO {

    private String token;

    public JwtResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
