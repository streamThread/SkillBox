package main;

import main.response.Action;

import java.util.*;

public class Storage {

    private static final Map<Integer, Action> actionStorage = Collections.synchronizedMap(new TreeMap<>());
    private static int ID = 1;

    private Storage() {
    }

    public static synchronized Map<Integer, Action> getAllActionsFromStorage() {
        return new HashMap<>(actionStorage);
    }

    public static synchronized Map<Integer, Action> getAllActionsPaginated(Integer start, Integer size) {
        SortedMap<Integer, Action> actionMap = new TreeMap<>(actionStorage);
        if (size >= actionMap.size()) {
            return actionMap;
        }
        return actionMap.subMap(start, start + size);
    }

    public static synchronized Integer putActionInStorage(Action action) {
        actionStorage.put(ID++, action);
        return ID - 1;
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

    public static Action replaceActionInStorage(Integer id, Action action) {
        return actionStorage.put(id, action);
    }
}
