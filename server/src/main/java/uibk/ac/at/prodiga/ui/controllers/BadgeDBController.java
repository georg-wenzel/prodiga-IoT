package uibk.ac.at.prodiga.ui.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BadgeDB;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.BadgeDBRepository;
import uibk.ac.at.prodiga.services.BadgeDBService;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.io.Serializable;
import java.util.Collection;

/**
 * Controller for BadgeDBService
 */
@Component
@Scope("view")
public class BadgeDBController implements Serializable
{
    private static final long serialVersionUID = 2523502304205602054L;

    private final BadgeDBService badgeDBService;
    private final ProdigaUserLoginManager prodigaUserLoginManager;

    public BadgeDBController(BadgeDBService badgeDBService, ProdigaUserLoginManager prodigaUserLoginManager) {
        this.badgeDBService = badgeDBService;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
    }

    public Collection<BadgeDB> getBadgesByUser(){
        return this.badgeDBService.getAllBadgesByUser(prodigaUserLoginManager.getCurrentUser());
    }

    public int getBadgesByUserNum(){
        return getBadgesByUser().size();
    }

    public Collection<BadgeDB> getLastWeeksBadges(){
        return this.badgeDBService.getLastWeeksBadges();
    }

    public Collection<BadgeDB> getBadgesByDepartment() {
        return this.badgeDBService.getAllBadgesByDepartment(prodigaUserLoginManager.getCurrentUser().getAssignedDepartment());
    }

    public int getBadgesByDepartmentNum(){
        return getBadgesByDepartment().size();
    }

    public Collection<BadgeDB> getLastWeeksBadgesByUser(){
        return this.badgeDBService.getLastWeeksBadgesByUser(prodigaUserLoginManager.getCurrentUser());
    }
}
