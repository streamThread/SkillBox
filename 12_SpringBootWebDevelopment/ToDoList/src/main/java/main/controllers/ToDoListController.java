package main.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import main.Storage;
import main.response.Action;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Api(tags = "ToDo List")
@RequestMapping("/actions")
public class ToDoListController {

    @ApiOperation(value = "returns list of actions")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Integer, Action> getAllActions() {
        return Storage.getAllActionsFromStorage();
    }

    @ApiOperation(value = "returns action by requested id")
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> getAction(@RequestParam("id") int id) {
        Action action = Storage.getActionFromStorage(id);
        if (action != null) {
            return new ResponseEntity<>(action, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "add action to the list")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public int putAction(Action action) {
        return Storage.putActionInStorage(action);
    }

    @ApiOperation(value = "add action by id to the list (removes old action`s value by this id)")
    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> replaceAction(@RequestParam("id") int id, Action action) {
        if (id > Storage.getAllActionsFromStorage().size() + 1 || id < 1) {
            return ResponseEntity.badRequest().body(null);
        }
        if (Storage.replaceActionInStorage(id, action) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(null);
    }

    @ApiOperation(value = "delete action by id")
    @DeleteMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Action> deleteAction(@RequestParam("id") int id) {
        if (id > Storage.getAllActionsFromStorage().size() + 1 || id < 1) {
            return ResponseEntity.badRequest().body(null);
        }
        if (Storage.deleteActionFromStorage(id)) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.notFound().build();
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
