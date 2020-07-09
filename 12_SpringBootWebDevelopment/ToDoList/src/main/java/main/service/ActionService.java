package main.service;

import java.util.List;
import java.util.Optional;
import main.entity.Action;
import main.entity.User;

public interface ActionService {

  List<Action> getAllActions();

  List<Action> getAllActionsByUser(User user);

  List<Action> getActionsByPage(Integer pageNumber, Integer pageSize);

  Optional<Action> getAction(Long id);

  List<Action> getAllActionsByContent(String query);

  List<Action> getAllActionsByUserAndContent(User user, String query);

  List<Action> getAllActionsByContentByPage(String query, Integer pageNumber,
      Integer pageSize);

  Long addActionToDB(Action action);

  Long replaceActionToDBIfExists(Action action);

  boolean deleteActionIfExists(Long id);

  void deleteAllActions();
}
