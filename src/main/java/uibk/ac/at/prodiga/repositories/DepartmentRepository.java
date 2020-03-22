package uibk.ac.at.prodiga.repositories;
import uibk.ac.at.prodiga.model.Department;

/**
 * DB Repository for managing departments
 */
public interface DepartmentRepository extends AbstractRepository<Department, Long>
{
    //Magic methods
    Department findFirstById(Long id);
    Department findFirstByName(String name);
}
