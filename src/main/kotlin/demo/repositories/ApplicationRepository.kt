package demo.repositories;

import demo.models.offers.Application;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ApplicationRepository: CrudRepository<Application, Int> {
}
