package main.model;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends CrudRepository<Action, Integer> {

  Page<Action> findBy(Pageable pageable);

  List<Action> findAllByOrderById();

  Optional<List<Action>> findByContentContaining(String query,
      Pageable pageable);

  Optional<List<Action>> findByContentContaining(String query, Sort sort);
}
