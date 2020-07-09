package main.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.util.List;
import main.entity.Action;
import main.entity.User;
import main.entity.dto.PutActionDTO;
import main.entity.dto.ReplaceActionDTO;
import main.service.ActionService;
import main.util.View;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Api(tags = "ToDo List")
@RequestMapping("/actions/")
public class ToDoListController {

  private final ActionService actionService;

  HttpHeaders httpHeaders = new HttpHeaders();

  ToDoListController(ActionService actionService) {
    httpHeaders.setCacheControl(CacheControl.noCache());
    httpHeaders.setPragma("no-cache");
    this.actionService = actionService;
  }

  @JsonView(View.ActionWithOwnerLogin.class)
  @ApiOperation(value =
      "returns list of actions. Simple pagination available. You can optionally set the "
          +
          "start and sample size. Also you can search text (if action contains that text)")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<Action>> getAction(@ApiParam(
      value = "text to search in the actions of ToDo list")
  @RequestParam(required = false) String query,
      @ApiParam(value = "Number of page that you want to see. " +
          "Numerate starts with zero")
      @RequestParam(required = false) Integer pageNumber,
      @ApiParam(value = "Action's count on page")
      @RequestParam(required = false) Integer pageSize,
      @ApiIgnore
      @AuthenticationPrincipal User user) {
    List<Action> actions;
    if (pageNumber == null || pageSize == null) {
      if (query == null) {
        actions = actionService.getAllActionsByUser(user);
      } else {
        actions = actionService.getAllActionsByUserAndContent(user, query);
      }
    } else {
      if (query != null) {
        actions = actionService
            .getAllActionsByContentByPage(query, pageNumber, pageSize);
      } else {
        actions = actionService.getActionsByPage(pageNumber, pageSize);
      }
    }
    return ResponseEntity.ok(actions);
  }

  @ApiOperation(value = "returns action by requested id")
  @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Action> getAction(
      @ApiParam(value = "id of the search action", required = true)
      @PathVariable Long id) {
    return actionService.getAction(id)
        .map(action -> new ResponseEntity<>(action, httpHeaders, HttpStatus.OK))
        .orElse(ResponseEntity.notFound().build());
  }

  @ApiOperation(value = "add action to the list")
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> putAction(
      @ApiParam(value = "Content and time of adding an action", required = true)
      @RequestBody PutActionDTO putActionDTO,
      @ApiIgnore @AuthenticationPrincipal User user
  ) {
    Action action = new Action();
    action.setContent(putActionDTO.getContent());
    action.setTimeStamp(putActionDTO.getTimeStamp());
    action.setOwner(user);
    Long id = actionService.addActionToDB(action);
    return ResponseEntity.created(URI.create(String.format("/actions/%d", id)))
        .build();
  }

  @ApiOperation(value = "add action by id to the list (removes old action`s value by this id)")
  @PutMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Long> replaceAction(
      @ApiParam(value = "id, content and adding time of the new action to add", required = true)
      @RequestBody ReplaceActionDTO replaceActionDTO) {
    Action action = new Action();
    action.setId(replaceActionDTO.getId());
    action.setContent(replaceActionDTO.getContent());
    action.setTimeStamp(replaceActionDTO.getTimeStamp());
    Long savedId = actionService.replaceActionToDBIfExists(action);
    return savedId != 0 ?
        ResponseEntity
            .created(URI.create(String.format("/actions/%d", savedId)))
            .body(savedId) :
        ResponseEntity.notFound().build();
  }

  @ApiOperation(value = "delete action by id")
  @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Action> deleteAction(
      @ApiParam(value = "id of the action to delete", required = true)
      @PathVariable Long id) {
    return actionService.deleteActionIfExists(id) ?
        ResponseEntity.ok().body(null) :
        ResponseEntity.notFound().build();
  }

  @ApiOperation(value = "clear list of actions")
  @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Action> clearListOfActions() {
    actionService.deleteAllActions();
    return ResponseEntity.ok().body(null);
  }
}

