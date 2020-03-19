package uibk.ac.at.prodiga.services;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Team;
import uibk.ac.at.prodiga.repositories.TeamRepository;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Collection;

/**
 * Service for accessing and manipulating teams.
 */
@Component
@Scope("application")
public class TeamService
{
    private final TeamRepository teamRepository;
    private final UserService userService;
    private final ProdigaUserLoginManager userLoginManager;

    public TeamService(TeamRepository teamRepository, ProdigaUserLoginManager userLoginManager, UserService userService)
    {
        this.teamRepository = teamRepository;
        this.userLoginManager = userLoginManager;
        this.userService = userService;
    }

    /**
     * Returns a collection of all teams
     * @return A collection of all teams.
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Collection<Team> getAllTeams()
    {
        return Lists.newArrayList(teamRepository.findAll());
    }

    /**
     * Gets the FIRST team with the specified team name.
     * This may NOT return a unique result, as teams can have the same name across departments.
     * @param name The name of the team
     * @return The first team in the database which has this name, or null if none exists
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Team getFirstByName(String name)
    {
        return teamRepository.findFirstByName(name);
    }

    /**
     * Gets the first team with the specified id. (Unique identifier)
     * @param id The id of the team
     * @return The team with this Id, or null if none exists
     */
    @PreAuthorize("hasAuthority('DEPARTMENTLEADER')")
    public Team getFirstById(long id)
    {
        return teamRepository.findFirstById(id);
    }
}
