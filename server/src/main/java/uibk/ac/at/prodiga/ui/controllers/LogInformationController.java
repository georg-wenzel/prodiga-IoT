package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uibk.ac.at.prodiga.model.LogInformation;
import uibk.ac.at.prodiga.services.LogInformationService;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Component
@Scope("view")
public class LogInformationController implements Serializable {

    private static final long serialVersionUID = 5524368887622577315L;

    private final LogInformationService logInformationService;

    private List<LogInformation> result = null;
    private String user = null;
    private Date startDate = null;
    private Date endDate = null;

    public LogInformationController(LogInformationService logInformationService) {
        this.logInformationService = logInformationService;
    }

    public List<LogInformation> getResult() {
        return result;
    }

    public void setResult(List<LogInformation> result) {
        this.result = result;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void search() {
        if(!StringUtils.isEmpty(user) && startDate != null && endDate != null) {
            result = logInformationService.findAllForUserBetweenDates(user, startDate, endDate);
        } else if(!StringUtils.isEmpty(user) && startDate != null) {
            result = logInformationService.findAllForUserAfterDate(user, startDate);
        } else if(!StringUtils.isEmpty(user) && endDate != null) {
            result = logInformationService.findAllForUserBeforeDate(user, endDate);
        } else if(startDate != null && endDate != null) {
            result = logInformationService.findAllBetweenDates(startDate, endDate);
        } else if (startDate != null) {
            result = logInformationService.findAllAfterDate(startDate);
        } else if(endDate != null) {
            result = logInformationService.findAllBeforeDate(endDate);
        } else {
            result = logInformationService.findAll();
        }
    }

    public void reset() {
        startDate = null;
        endDate = null;
        user = null;
        result = null;
    }
}
