package uibk.ac.at.prodiga.rest.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class HistoryEntryDTO {

    @NotBlank(message = "Internal Id is mandatory")
    private String cubeInternalId;
    @Positive(message = "Side must be between 1 and 12")
    private int side;
    @Positive(message = "Seconds must be positive")
    private int seconds;


    public String getCubeInternalId() {
        return cubeInternalId;
    }

    public void setCubeInternalId(String cubeInternalId) {
        this.cubeInternalId = cubeInternalId;
    }

    public Integer getSide() {
        return side;
    }

    public void setSide(Integer side) {
        this.side = side;
    }

    public Integer getSeconds() {
        return seconds;
    }

    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }
}
