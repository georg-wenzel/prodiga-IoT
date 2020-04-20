package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.FrequencyType;
import uibk.ac.at.prodiga.services.MailFrequencyService;
import uibk.ac.at.prodiga.services.UserService;

import java.util.LinkedList;
import java.util.List;

@Component
@Scope("view")
public class FrequencyTypeController {

    private final MailFrequencyService mailFrequencyService;
    private final UserService userService;

    private FrequencyType currentFrequencyType;

    public FrequencyTypeController(MailFrequencyService mailFrequencyService, UserService userService) {
        this.mailFrequencyService = mailFrequencyService;
        this.userService = userService;
    }

    /**
     * Returns a collection of all vacations for the user that are ongoing or in the future
     * @return A collection of all vacations for the user that are ongoing or in the future
     */
    public FrequencyType getFrequencyTypeOfCurrentUser() {
        currentFrequencyType = userService.getFrequencyTypeOfCurrentUser();
        if(currentFrequencyType == null){
            currentFrequencyType = FrequencyType.DAILY;
        }
        return currentFrequencyType;
    }


    public List<String> getAllFrequencyTypesTotal() {
        List<String> freqencyTypeList = new LinkedList<>();
        freqencyTypeList.add(FrequencyType.DAILY.getLabel());
        freqencyTypeList.add(FrequencyType.WEEKLY.getLabel());
        freqencyTypeList.add(FrequencyType.MONTHLY.getLabel());
        return freqencyTypeList;
    }
}
