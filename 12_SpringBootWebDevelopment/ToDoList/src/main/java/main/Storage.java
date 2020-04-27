package main;

import main.response.Action;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Storage {

    private static final Map<Integer, Action> actionStorage = Collections.synchronizedMap(new TreeMap<>());
    private static int ID = 1;

    private Storage() {
    }

    public static synchronized Map<Integer, Action> getAllActionsFromStorage() {
        return new HashMap<>(actionStorage);
    }

    public static synchronized int putActionInStorage(Action action) {
        int tempID = ID;
        actionStorage.put(ID++, action);
        return tempID;
    }

    public static synchronized Action getActionFromStorage(Integer id) {
        if (actionStorage.containsKey(id)) {
            return actionStorage.get(id);
        }
        return null;
    }

    public static synchronized boolean deleteActionFromStorage(Integer id) {
        if (actionStorage.remove(id) != null) {
            for (int i = id; i < actionStorage.size() + 1; i++) {
                actionStorage.compute(i, (k, v) -> actionStorage.get(k + 1));
            }
            return true;
        }
        return false;
    }

    public static synchronized void clearStorage() {
        ID = 1;
        actionStorage.clear();
    }

    public static Action replaceActionInStorage(int id, Action action) {
        return actionStorage.put(id, action);
    }
}
