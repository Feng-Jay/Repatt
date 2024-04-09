package cofix.core.preprocess.statementRepair;


import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.Divider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

  private static Integer idPool = 0;
  private static final Map<Pair<String, Integer>, Integer> lineGroupMap = new HashMap<>();
  private static final Map<Integer, Group> groupMap = new HashMap<>();
  private final Integer id;
  private final List<Pair<String, Integer>> lines;
  private Integer status;//init 0


  public Group(List<Pair<String, Integer>> lines) {
    this.lines = lines;
    id = ++idPool;
    status = 0;
  }

  public static void buildMap(Subject subject) {
    List<List<Pair<String, Integer>>> groups = Divider.divide(subject);
    for (List<Pair<String, Integer>> group : groups) {
      Group g = new Group(group);
      for (Pair<String, Integer> line : group) {
        lineGroupMap.put(line, g.id);
        groupMap.put(g.id, g);
      }
    }
  }


  public static List<Pair<String, Integer>> checkLine(Pair<String, Integer> line) {
    Group group = groupMap.get(lineGroupMap.get(line));
    if (group == null) {
      return null;
    }
    if (group.status == 1) {
      return null;//此组已经应用过
    } else {
      group.status = 1;
      return group.lines;
    }
  }

}


