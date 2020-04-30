package main;

import main.response.Action;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Storage {

    private static final Map<Integer, Action> actionStorage = Collections.synchronizedMap(new TreeMap<>());
    private static int ID = 1;

    private Storage() {
    }

    public static synchronized Map<Integer, Action> getAllActionsFromStorage() {
        return new TreeMap<>(actionStorage);
    }

    public static synchronized Map<Integer, Action> getAllActionsPaginated(Integer start, Integer size) {
        TreeMap<Integer, Action> integerActionTreeMap = (TreeMap<Integer, Action>) getAllActionsFromStorage();
        if (start == null || size == null) {
            return integerActionTreeMap;
        }
        if (size >= integerActionTreeMap.size()) {
            return integerActionTreeMap;
        }
        return integerActionTreeMap.subMap(start, start + size);
    }

    public static synchronized Map<Integer, Action> getAllActionsBySearchString(String query) {
        return getAllActionsFromStorage().entrySet().stream()
                .filter(a -> a.getValue().getContent().contains(query))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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
