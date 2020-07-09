package jsonclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Result {

  private Map<String, List<String>> stations = new HashMap<>();
  private List<Line> lines = new ArrayList<>();
  private Set<List<Line>> connections = new HashSet<>();

  public Set<List<Line>> getConnections() {
    return connections;
  }

  public void setConnections(Set<List<Line>> connections) {
    this.connections = connections;
  }

  public List<Line> getLines() {
    return lines;
  }

  public void setLines(List<Line> lines) {
    this.lines = lines;
  }

  public Map<String, List<String>> getStations() {
    return stations;
  }

  public void setStations(Map<String, List<String>> stations) {
    this.stations = stations;
  }
}
