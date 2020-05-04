package main.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import main.model.Action;
import main.model.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@Api(tags = "ToDo List")
@RequestMapping("/actions/")
public class ToDoListController {

    private final ActionRepository actionRepository;

    HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    ToDoListController(ActionRepository actionRepository) {
        httpHeaders.setCacheControl(CacheControl.noCache());
        httpHeaders.setPragma("no-cache");
        this.actionRepository = actionRepository;
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
            return ResponseEntity.ok(actionRepository.findAllByOrderById());
        }
        return ResponseEntity.ok()
                .body(actionRepository.findBy(PageRequest.of(pageNumber, pageSize, Sort.by("id"))).getContent());
    }

    @ApiOperation(value = "returns action by requested id")
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> getAction(@ApiParam(value = "id of the search action", required = true)
                                            @PathVariable Integer id) {
        return actionRepository.findById(id)
                .map(action -> new ResponseEntity<>(action, httpHeaders, HttpStatus.OK))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
        if (pageNumber == null || pageSize == null) {
            return actionRepository.findByContentContaining(query, Sort.by("id"))
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return actionRepository.findByContentContaining(query, PageRequest.of(pageNumber, pageSize, Sort.by("id")))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiOperation(value = "add action to the list")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> putAction(@ApiParam(value = "content of the new action to add", required = true)
                                             @RequestParam String content,
                                             @ApiParam(value = "adding time of the new action", required = true)
                                             @RequestParam Long timeStamp) {
        Integer id = actionRepository.save(new Action(content, timeStamp)).getId();
        return ResponseEntity.created(URI.create(String.format("/actions/%d", id))).body(id);
    }

    @ApiOperation(value = "add action by id to the list (removes old action`s value by this id)")
    @PutMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> replaceAction(@ApiParam(value = "id of the new action to add", required = true)
                                                @PathVariable Integer id,
                                                @ApiParam(value = "content of the new action", required = true)
                                                @RequestParam String content,
                                                @ApiParam(value = "adding time of the new action", required = true)
                                                @RequestParam Long timeStamp) {
        if (actionRepository.existsById(id)) {
            actionRepository.save(new Action(id, content, timeStamp));
            return ResponseEntity.created(URI.create(String.format("/actions/%d", id))).body(null);
        }
        return ResponseEntity.notFound().build();
    }


    @ApiOperation(value = "delete action by id")
    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> deleteAction(@ApiParam(value = "id of the action to delete", required = true)
                                               @PathVariable Integer id) {
        if (actionRepository.existsById(id)) {
            actionRepository.deleteById(id);
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "clear list of actions")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> clearListOfActions() {
        actionRepository.deleteAll();
        return ResponseEntity.ok().body(null);
    }
}

