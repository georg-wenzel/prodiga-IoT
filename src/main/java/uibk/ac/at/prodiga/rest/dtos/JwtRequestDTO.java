package uibk.ac.at.prodiga.rest.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class JwtRequestDTO {


    @NotBlank(message = "Internal Id is mandatory")
    @NotNull
    private String internalId;

    @NotBlank(message = "Password is mandatory")
    @NotNull
    private String password;

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
