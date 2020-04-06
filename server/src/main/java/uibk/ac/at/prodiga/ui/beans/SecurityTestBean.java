package uibk.ac.at.prodiga.ui.beans;

import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class SecurityTestBean {

    private boolean showOkDialog = false;
    private String performedAction = "NONE";

    public boolean isShowOkDialog() {
        return showOkDialog;
    }

    public String getPerformedAction() {
        return performedAction;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void doAdminAction() {
        performedAction = "ADMIN";
        showOkDialog = true;
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    public void doManagerAction() {
        performedAction = "MANAGER";
        showOkDialog = true;
    }

    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public void doEmployeeAction() {
        performedAction = "EMPLOYEE";
        showOkDialog = true;
    }

    public void doHideOkDialog() {
        showOkDialog = false;
    }

}
