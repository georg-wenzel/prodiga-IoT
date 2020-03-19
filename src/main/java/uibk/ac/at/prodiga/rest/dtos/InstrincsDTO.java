package uibk.ac.at.prodiga.rest.dtos;

import java.util.List;

public class InstrincsDTO {

    private String ipAddress;
    private List<CubeInstrincs> cubeInstrincs;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<CubeInstrincs> getCubeInstrincs() {
        return cubeInstrincs;
    }

    public void setCubeInstrincs(List<CubeInstrincs> cubeInstrincs) {
        this.cubeInstrincs = cubeInstrincs;
    }
}
