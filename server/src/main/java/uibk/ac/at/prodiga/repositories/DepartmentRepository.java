package uibk.ac.at.prodiga.repositories;
import org.springframework.data.domain.Persistable;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.model.Department;
import uibk.ac.at.prodiga.model.Dice;
import uibk.ac.at.prodiga.model.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * DB Repository for managing departments
 */
public interface DepartmentRepository extends AbstractRepository<Department, Long>
{
    //Magic methods
    Department findFirstById(Long id);
    Department findFirstByName(String name);
}
