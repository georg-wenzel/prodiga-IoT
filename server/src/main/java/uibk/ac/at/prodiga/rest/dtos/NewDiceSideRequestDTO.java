package uibk.ac.at.prodiga.rest.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class NewDiceSideRequestDTO {

    @NotBlank(message = "Internal Id is mandatory")
    @NotNull
    private String internalId;

    private int side;

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }
}
