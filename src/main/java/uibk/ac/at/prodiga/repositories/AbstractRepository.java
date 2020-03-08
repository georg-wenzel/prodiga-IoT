package uibk.ac.at.prodiga.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface AbstractRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {

}
