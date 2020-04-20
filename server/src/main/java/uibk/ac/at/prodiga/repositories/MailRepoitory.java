package uibk.ac.at.prodiga.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uibk.ac.at.prodiga.model.FrequencyType;
import uibk.ac.at.prodiga.model.User;

import java.util.List;

public interface MailRepoitory {

    @Query("SELECT u FROM User u WHERE u.frequencyType=:frequencyType")
    List<User> findUserByFrequencyType(@Param("frequencyType") FrequencyType frequencyType);
}
