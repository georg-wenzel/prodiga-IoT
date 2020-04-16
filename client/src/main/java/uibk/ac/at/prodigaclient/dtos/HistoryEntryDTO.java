package uibk.ac.at.prodigaclient.dtos;

public class HistoryEntryDTO {

    private String cubeInternalId;
    private int side;
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