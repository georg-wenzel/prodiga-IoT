package uibk.ac.at.prodigaclient.dtos;

public class NewDiceSideRequestDTO {

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
