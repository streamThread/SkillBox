package main.repos;

import main.entity.Action;
import main.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends CrudRepository<Action, Long> {

    Page<Action> findBy(Pageable pageable);

    List<Action> findAllByOrderById();

    List<Action> findAllByOwnerOrderByIdDesc(User user);

    List<Action> findByContentContaining(String query, Pageable pageable);

    List<Action> findByContentContaining(String query, Sort sort);

    List<Action> findByContentContainingAndOwnerOrderByIdDesc(String query, User user);
}
