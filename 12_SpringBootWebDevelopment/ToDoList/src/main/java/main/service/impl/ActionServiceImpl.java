package main.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import main.entity.Action;
import main.entity.User;
import main.entity.dto.GetActionDTO;
import main.repos.ActionRepository;
import main.service.ActionService;
import main.util.Convert;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ActionServiceImpl implements ActionService {

  private final ActionRepository actionRepository;
  private final Sort sortById = Sort.by("id");

  public ActionServiceImpl(ActionRepository actionRepository) {
    this.actionRepository = actionRepository;
  }

  @Override
  public List<Action> getAllActions() {
    return actionRepository.findAllByOrderById();
  }

  @Override
  public List<GetActionDTO> getAllActionsByUser(User user) {
    return actionRepository.findAllByOwnerOrderByIdDesc(user).stream()
        .map(a -> new GetActionDTO(a.getId(), a.getContent(),
            Convert.getDateAsString(a.getCreationTime())))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public List<GetActionDTO> getActionsByPage(Integer pageNumber,
      Integer pageSize) {
    return actionRepository
        .findBy(PageRequest.of(pageNumber, pageSize, sortById)).getContent()
        .stream()
        .map(a -> new GetActionDTO(a.getId(), a.getContent(),
            Convert.getDateAsString(a.getCreationTime())))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public Optional<Action> getAction(Long id) {
    return actionRepository.findById(id);
  }

  @Override
  public List<Action> getAllActionsByContent(String query) {
    return actionRepository.findByContentContaining(query, sortById);
  }

  @Override
  public List<GetActionDTO> getAllActionsByUserAndContent(User user,
      String query) {
    return actionRepository
        .findByContentContainingAndOwnerOrderByIdDesc(query, user).stream()
        .map(a -> new GetActionDTO(a.getId(), a.getContent(),
            Convert.getDateAsString(a.getCreationTime())))
        .collect(Collectors.toList());
  }

  @Override
  public List<GetActionDTO> getAllActionsByContentByPage(String query,
      Integer pageNumber, Integer pageSize) {
    return actionRepository.findByContentContaining(query,
        PageRequest.of(pageNumber, pageSize, sortById)).stream()
        .map(a -> new GetActionDTO(a.getId(), a.getContent(),
            Convert.getDateAsString(a.getCreationTime())))
        .collect(Collectors.toList());
  }

  @Override
  public Long addActionToDB(Action action) {
    return actionRepository.save(action).getId();
  }

  @Override
  public Long replaceActionToDBIfExists(Action action) {
    return actionRepository.existsById(action.getId()) ?
        actionRepository.save(action).getId() :
        Long.valueOf(0);
  }

  @Override
  public boolean deleteActionIfExists(Long id) {
    boolean isDeleted = false;
    if (actionRepository.existsById(id)) {
      actionRepository.deleteById(id);
      isDeleted = true;
    }
    return isDeleted;
  }

  @Override
  public void deleteAllActions() {
    actionRepository.deleteAll();
  }
}
