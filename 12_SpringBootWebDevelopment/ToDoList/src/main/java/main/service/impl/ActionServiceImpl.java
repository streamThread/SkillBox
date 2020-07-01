package main.service.impl;

import main.entity.Action;
import main.repos.ActionRepository;
import main.service.ActionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public List<Action> getActionsByPage(Integer pageNumber, Integer pageSize) {
        return actionRepository.findBy(PageRequest.of(pageNumber, pageSize, sortById)).getContent();
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
    public List<Action> getAllActionsByContentByPage(String query, Integer pageNumber, Integer pageSize) {
        return actionRepository.findByContentContaining(query, PageRequest.of(pageNumber, pageSize, sortById));
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
