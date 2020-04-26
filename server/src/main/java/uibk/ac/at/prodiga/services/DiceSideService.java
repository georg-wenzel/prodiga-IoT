package uibk.ac.at.prodiga.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.DiceSide;
import uibk.ac.at.prodiga.repositories.DiceSideRepository;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.util.Collection;
import java.util.Date;

@Component
@Scope("application")
public class DiceSideService {

    private final DiceSideRepository diceSideRepository;
    private final ProdigaUserLoginManager prodigaUserLoginManager;

    public DiceSideService(DiceSideRepository diceSideRepository, ProdigaUserLoginManager prodigaUserLoginManager) {
        this.diceSideRepository = diceSideRepository;
        this.prodigaUserLoginManager = prodigaUserLoginManager;
    }

    /**
     * Saves the given DiceSide
     * @param ds The diceSide to save
     * @return The newly saved dice side
     */
    public DiceSide save(DiceSide ds) {
        if(ds.isNew()) {
            ds.setCurrentSeconds(0);
            ds.setObjectCreatedDateTime(new Date());
            ds.setObjectCreatedUser(prodigaUserLoginManager.getCurrentUser());
        } else {
            DiceSide dbDiceSide = diceSideRepository.findFirstByDiceAndSide(ds.getDice(), ds.getSide());

            if(dbDiceSide != null) {
                dbDiceSide.setBookingCategory(ds.getBookingCategory());
                ds = dbDiceSide;
            }
            ds.setObjectChangedDateTime(new Date());
            ds.setObjectChangedUser(prodigaUserLoginManager.getCurrentUser());
        }

        return diceSideRepository.save(ds);
    }

    public DiceSide findByDiceAndSide(Dice dice, Integer side){
        return diceSideRepository.findFirstByDiceAndSide(dice, side);
    }

    public Collection<DiceSide> findByDice(Dice dice)
    {
        return diceSideRepository.findAllByDice(dice);
    }
}
