package uibk.ac.at.prodiga.repositories;

        import org.springframework.data.jpa.repository.Query;
        import org.springframework.data.repository.query.Param;
        import uibk.ac.at.prodiga.model.*;

        import java.util.Collection;
        import java.util.List;

public interface DiceRepository extends AbstractRepository<Dice, Long>
{

    Dice findFirstById(long id);

    /**
     * Returns all dices which are  assigned to the given raspi
     * @param raspi The raspi to search for
     * @return A list with all dices
     */
    List<Dice> findAllByAssignedRaspberry(RaspberryPi raspi);

    /**
     * Returns the dice which is used by the given user
     * @param u The user to search for
     * @return The dice assigned to the given user
     */
    Dice findFirstByUser(User u);

    /**
     * Returns the dice with the given internal id
     * @param internalId The internal id
     * @return The found dice
     */
    Dice findFirstByInternalId(String internalId);

    /**
     * Returns all dice where the corresponding user is part of the given team
     * @param team The team to search for
     * @return All dice belonging to users of that team
     */
    @Query("Select ds.dice FROM DiceSide ds WHERE " +
            "ds.dice.user.assignedTeam = :team AND ds.bookingCategory = :category")
    Collection<Dice> findDiceByUserTeamAndCategory(@Param("team") Team team, @Param("category") BookingCategory category);
}
