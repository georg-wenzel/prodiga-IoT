package uibk.ac.at.prodiga.ui.controllers;


import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.RaspberryPi;
import uibk.ac.at.prodiga.services.RaspberryPiService;
import uibk.ac.at.prodiga.utils.ConfigDownloader;
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.SnackbarHelper;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Collection;
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

    private String passwordForDownload;
    private RaspberryPi raspberryPiToDownload;
    private StreamedContent config;

    public RaspberryPiController(RaspberryPiService raspberryPiService) {
        this.raspberryPiService = raspberryPiService;
    }

    /**
     * Returns all raspberry pis which are not configured
     * @return A list of raspberry pis
     */
    public Collection<RaspberryPi> getAllPendingRaspberryPis() {
        if(pendingRaspis == null) pendingRaspis = this.raspberryPiService.getAllPendingRaspberryPis();
        return pendingRaspis;
    }

    /**
     * Returns a list of all raspberry pis which are configured
     * @return A list of raspberry pis
     */
    public Collection<RaspberryPi> getAllConfiguredRaspberryPis() {
        if(configuredRaspis == null) configuredRaspis = this.raspberryPiService.getAllConfiguredRaspberryPis();
        return configuredRaspis;
    }

    /**
     * Deletes the given raspi from all pending
     * @param raspi The raspi to delte
     */
    public void deletePendingRaspberry(RaspberryPi raspi) {
        if(raspi == null) {
            return;
        }
        raspberryPiService.tryDeletePendingRaspberry(raspi);
        pendingRaspis = null;
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
        configuredRaspis = null;
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

    public String getPasswordForDownload() {
        return passwordForDownload;
    }

    public void setPasswordForDownload(String passwordForDownload) {
        this.passwordForDownload = passwordForDownload;
    }

    public RaspberryPi getRaspberryPiToDownload() {
        return raspberryPiToDownload;
    }

    public void setRaspberryPiToDownload(RaspberryPi raspberryPiToDownload) {
        this.raspberryPiToDownload = raspberryPiToDownload;
    }

    public RaspberryPi getRaspberryPi() {
        return raspberryPi;
    }

    public void setRaspberryPi(RaspberryPi raspberryPi) {
        this.raspberryPi = raspberryPi;
    }

    public StreamedContent getConfig() throws Exception {
        if(raspberryPiToDownload != null) {
            DefaultStreamedContent content = new DefaultStreamedContent();
            content.setName(raspberryPiToDownload.getInternalId() + ".zip");
            content.setContentType("application/zip");
            content.setStream(new FileInputStream(ConfigDownloader.downloadConfig(passwordForDownload, raspberryPiToDownload.getInternalId()).getAbsolutePath()));
            return content;
        }
        return null;
    }

    public void setConfig(StreamedContent config) {
        this.config = config;
    }
}
