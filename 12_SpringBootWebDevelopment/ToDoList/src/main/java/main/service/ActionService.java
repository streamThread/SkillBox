package main.service;

import java.util.List;
import java.util.Optional;
import main.entity.Action;
import main.entity.User;
import main.entity.dto.GetActionDTO;

public interface ActionService {

  List<Action> getAllActions();

  List<GetActionDTO> getAllActionsByUser(User user);

  List<GetActionDTO> getActionsByPage(Integer pageNumber, Integer pageSize);

  Optional<Action> getAction(Long id);

  List<Action> getAllActionsByContent(String query);

  List<GetActionDTO> getAllActionsByUserAndContent(User user, String query);

  List<GetActionDTO> getAllActionsByContentByPage(String query,
      Integer pageNumber,
      Integer pageSize);

  Long addActionToDB(Action action);

  Long replaceActionToDBIfExists(Action action);

  boolean deleteActionIfExists(Long id);

  void deleteAllActions();
}
