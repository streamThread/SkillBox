package main.service;

import main.entity.Action;

import java.util.List;
import java.util.Optional;

public interface ActionService {

    List<Action> getAllActions();

    List<Action> getActionsByPage(Integer pageNumber, Integer pageSize);

    Optional<Action> getAction(Long id);

    List<Action> getAllActionsByContent(String query);

    List<Action> getAllActionsByContentByPage(String query, Integer pageNumber, Integer pageSize);

    Long addActionToDB(Action action);

    Long replaceActionToDBIfExists(Action action);

    boolean deleteActionIfExists(Long id);

    void deleteAllActions();
}
