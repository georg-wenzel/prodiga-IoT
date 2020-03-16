package uibk.ac.at.prodiga.rest.dtos;

import javax.validation.constraints.NotBlank;

public class JwtRequestDTO {


    @NotBlank(message = "Internal Id is mandatory")
    private String internalID;

    @NotBlank(message = "Password is mandatory")
    private String password;

    public String getInternalID() {
        return internalID;
    }

    public void setInternalID(String internalID) {
        this.internalID = internalID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
