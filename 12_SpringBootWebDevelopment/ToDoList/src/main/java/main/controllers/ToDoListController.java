package main.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import main.Storage;
import main.response.Action;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@Api(tags = "ToDo List")
@RequestMapping("/actions/")
public class ToDoListController {

    HttpHeaders httpHeaders = new HttpHeaders();

    {
        httpHeaders.setCacheControl(CacheControl.noCache());
        httpHeaders.setPragma("no-cache");
    }

    @ApiOperation(value = "returns list of actions. Simple pagination available. You can optionally set the start and sample size")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Integer, Action>> getAllActions(@RequestParam(value = "start", required = false) Integer start,
                                                              @RequestParam(value = "size", required = false) Integer size) {
        if (start == null || size == null) {
            return ResponseEntity.ok(Storage.getAllActionsFromStorage());
        }
        if (start < 1 || size < 1) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(Storage.getAllActionsPaginated(start, size));
    }

    @ApiOperation(value = "returns action by requested id")
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> getAction(@PathVariable Integer id) {
        Action action = Storage.getActionFromStorage(id);
        if (action != null) {
            return new ResponseEntity<>(action,
                    httpHeaders,
                    HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "add action to the list")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> putAction(Action action) {
        Integer id = Storage.putActionInStorage(action);
        return ResponseEntity.created(URI.create(String.format("/actions/%d", id))).body(id);
    }

    @ApiOperation(value = "add action by id to the list (removes old action`s value by this id)")
    @PutMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> replaceAction(@PathVariable Integer id, Action action) {
        synchronized (Storage.class) {
            if (id > Storage.getAllActionsFromStorage().size() + 1 || id < 1) {
                return ResponseEntity.badRequest().body(null);
            }
            if (Storage.replaceActionInStorage(id, action) == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        }
    }

    @ApiOperation(value = "delete action by id")
    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> deleteAction(@PathVariable Integer id) {
        synchronized (Storage.class) {
            if (id > Storage.getAllActionsFromStorage().size() + 1 || id < 1) {
                return ResponseEntity.badRequest().body(null);
            }
            if (Storage.deleteActionFromStorage(id)) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @ApiOperation(value = "clear list of actions")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> clearListOfActions() {
        synchronized (Storage.class) {
            Storage.clearStorage();
            if (Storage.getAllActionsFromStorage().isEmpty()) {
                return ResponseEntity.ok().body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
