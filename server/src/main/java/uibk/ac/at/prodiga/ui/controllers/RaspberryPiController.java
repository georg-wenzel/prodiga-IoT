package uibk.ac.at.prodiga.ui.controllers;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.services.RaspberryPiService;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Scope("view")
public class RaspberryPiController implements Serializable {

    private static final long serialVersionUID = 5325690687692577315L;

    private Collection<RaspberryPi> configuredRaspis;
    private Collection<RaspberryPi> pendingRaspis;

    private final RaspberryPiService raspberryPiService;

    private RaspberryPi raspberryPi;

    private String pendingRasPiInternalId;

    public RaspberryPiController(RaspberryPiService raspberryPiService) {
        this.raspberryPiService = raspberryPiService;
    }


    /**
     * Finds the raspberry pi by the given internal id
     * @param internalId The internal id
     * @return An Optional with the found raspberry
     */
    public Optional<RaspberryPi> findByInternalIdWithAuth(String internalId){
        return this.raspberryPiService.findByInternalIdWithAuth(internalId);
    }

    /**
     * Finds the given raspberry and throws a exception when it could not be found
     * @param internalId The given internal id
     * @return The raspberry
     * @throws Exception Exception which is thrown when the raspberry could not be found
     */

    public RaspberryPi findByInternalIdWithAuthAndThrow(String internalId) throws Exception {
        return this.raspberryPiService.findByInternalIdWithAuthAndThrow(internalId);
    }

    /**
     * Finds the raspberry pi by the given internal id
     * @param internalId The internal id
     * @return An Optional with the found raspberry
     */
    public Optional<RaspberryPi> findByInternalId(String internalId) {
        return this.raspberryPiService.findByInternalId(internalId);
    }

    /**
     * Finds the given raspberry and throws a exception when it could not be found
     * @param internalId The given internal id
     * @return The raspberry
     * @throws Exception Exception which is thrown when the raspberry could not be found
     */
    public RaspberryPi findByInternalIdAndThrow(String internalId) throws Exception {
        return this.raspberryPiService.findByInternalIdAndThrow(internalId);
    }

    /**
     * Returns all raspberry pis which are not configured
     * @return A list of raspberry pis
     */
    public Collection<RaspberryPi> getAllPendingRaspberryPis() {

        if(pendingRaspis == null) pendingRaspis = this.raspberryPiService.getAllPendingRaspberryPis();;
        return pendingRaspis;
    }

    /**
     * Returns a list of all raspberry pis which are configured
     * @return A list of raspberry pis
     */
    public Collection<RaspberryPi> getAllConfiguredRaspberryPis() {
        if(this.configuredRaspis == null) configuredRaspis = this.raspberryPiService.getAllConfiguredRaspberryPis();
        return configuredRaspis;
    }

    /**
     * Adds a new raspberry to the pending list
     *
     */
    public void addPendingRaspberry() {
        this.raspberryPiService.tryAddPendingRaspberry(this.pendingRasPiInternalId);
        //refresh data
        this.pendingRaspis = null;
        getAllPendingRaspberryPis();
    }

    /**
     * Action to save the currently displayed raspi.
     */
    public void doSaveRaspi() throws Exception {
        raspberryPi = this.raspberryPiService.save(raspberryPi);
        SnackbarHelper.getInstance()
                .showSnackBar("Raspberry Pi " + raspberryPi.getInternalId() + " saved!", MessageType.INFO);
    }

    /**
     * Deletes the given Raspberry Pi
     * @param raspi The raspi to delete
     */
    public void delete(RaspberryPi raspi) throws Exception {
        this.raspberryPiService.delete(raspi);
    }

    public void createRaspiByInternalId(String internalId) {
        this.raspberryPi = raspberryPiService.createRaspi(internalId);
    }


    public void setRaspyByInternal(String internalId) {
        createRaspiByInternalId(internalId);
    }

    public String getRaspyByInternal() {
        if (raspberryPi == null){
            return null;
        }
        else{
            return raspberryPi.getInternalId();
        }
    }

    public void loadRaspiById(Long raspiId) throws Exception {
        this.raspberryPi = raspberryPiService.findById(raspiId);
    }

    public void setRaspyById(Long id) throws Exception {
        loadRaspiById(id);
    }

    public Long getRaspyById() {
        if (raspberryPi == null){
            return null;
        }
        else{
            return raspberryPi.getId();
        }
    }


    public String getPendingRasPiInternalId() {
        return pendingRasPiInternalId;
    }

    public void setPendingRasPiInternalId(String pendingRasPiInternalId) {
        this.pendingRasPiInternalId = pendingRasPiInternalId;
    }

    public RaspberryPi getRaspberryPi() {
        return raspberryPi;
    }

    public void setRaspberryPi(RaspberryPi raspberryPi) {
        this.raspberryPi = raspberryPi;
    }
}
