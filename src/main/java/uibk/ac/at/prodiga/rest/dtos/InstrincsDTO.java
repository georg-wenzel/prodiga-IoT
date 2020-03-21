package uibk.ac.at.prodiga.rest.dtos;

import java.util.List;

public class InstrincsDTO {

    private String internalId;
    private List<CubeInstrincs> cubeInstrincs;

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public List<CubeInstrincs> getCubeInstrincs() {
        return cubeInstrincs;
    }

    public void setCubeInstrincs(List<CubeInstrincs> cubeInstrincs) {
        this.cubeInstrincs = cubeInstrincs;
    }
}
