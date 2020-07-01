package main.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import main.entity.Action;
import main.service.ActionService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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

    @ApiOperation(value = "returns list of actions. Simple pagination available. You can optionally set the " +
            "start and sample size")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Action>> getAllActions(@ApiParam(value = "Number of page that you want to see. " +
            "Numerate starts with zero")
                                                      @RequestParam(required = false) Integer pageNumber,
                                                      @ApiParam(value = "Action's count on page")
                                                      @RequestParam(required = false) Integer pageSize) {
        if (pageNumber == null || pageSize == null) {
            return ResponseEntity.ok(actionService.getAllActions());
        }
        return ResponseEntity.ok()
                .body(actionService.getActionsByPage(pageNumber, pageSize));
    }

    @ApiOperation(value = "returns action by requested id")
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> getAction(@ApiParam(value = "id of the search action", required = true)
                                            @PathVariable Long id) {
        return actionService.getAction(id)
                .map(action -> new ResponseEntity<>(action, httpHeaders, HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiOperation(value = "returns action by search text (if action contains that text)")
    @GetMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Action>> getAction(@ApiParam(
            value = "text to search in the actions of ToDo list", required = true)
                                                  @RequestParam String query,
                                                  @ApiParam(value = "Number of page that you want to see. " +
                                                          "Numerate starts with zero")
                                                  @RequestParam(required = false) Integer pageNumber,
                                                  @ApiParam(value = "Action's count on page")
                                                  @RequestParam(required = false) Integer pageSize) {
        List<Action> actions;
        if (pageNumber == null || pageSize == null) {
            actions = actionService.getAllActionsByContent(query);
        } else {
            actions = actionService.getAllActionsByContentByPage(query, pageNumber, pageSize);
        }
        return actions.isEmpty() ?
                ResponseEntity.notFound().build() :
                ResponseEntity.ok(actions);
    }

    @ApiOperation(value = "add action to the list")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> putAction(@ApiParam(value = "content of the new action to add", required = true)
                                          @RequestParam String content,
                                          @ApiParam(value = "adding time of the new action", required = true)
                                          @RequestParam Long timeStamp) {
        Long id = actionService.addActionToDB(new Action(content, timeStamp));
        return ResponseEntity.created(URI.create(String.format("/actions/%d", id))).body(id);
    }

    @ApiOperation(value = "add action by id to the list (removes old action`s value by this id)")
    @PutMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> replaceAction(@ApiParam(value = "id of the new action to add", required = true)
                                              @PathVariable Long id,
                                              @ApiParam(value = "content of the new action", required = true)
                                              @RequestParam String content,
                                              @ApiParam(value = "adding time of the new action", required = true)
                                              @RequestParam Long timeStamp) {
        Action action = new Action(id, content, timeStamp);
        Long savedId = actionService.replaceActionToDBIfExists(action);
        return savedId != 0 ?
                ResponseEntity.created(URI.create(String.format("/actions/%d", savedId))).body(savedId) :
                ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "delete action by id")
    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> deleteAction(@ApiParam(value = "id of the action to delete", required = true)
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

