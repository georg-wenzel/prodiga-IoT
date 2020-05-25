package uibk.ac.at.prodigaclient.dtos;


import java.util.List;

public class IntrinsicsDTO {

    private String internalId;
    private List<CubeIntrinsicsDTO> cubeIntrinsics;

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public List<CubeIntrinsicsDTO> getCubeIntrinsics() {
        return cubeIntrinsics;
    }

    public void setCubeIntrinsics(List<CubeIntrinsicsDTO> cubeIntrinsics) {
        this.cubeIntrinsics = cubeIntrinsics;
    }
}

