package cofix.core.preprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class TreeNode {

  private int _freq = 0;
  private final Map<Integer, TreeNode> _edges = new HashMap<>();

  public TreeNode() {
  }

  public void find(Queue<Integer> sequence, boolean exist, TreeNode mirror) {
    _freq += (exist ? 0 : 1);
    if (!sequence.isEmpty()) {
      Integer top = sequence.poll();
      TreeNode node = _edges.get(top);
      if (node == null) {
        node = new TreeNode();
        _edges.put(top, node);
      }
      TreeNode mirrorNode = mirror._edges.get(top);
      exist = true;
      if (mirrorNode == null) {
        exist = false;
        mirrorNode = new TreeNode();
        mirror._edges.put(top, mirrorNode);
      }

      node.find(sequence, exist, mirrorNode);
    }
  }

  public Map<Integer, TreeNode> getEdges() {
    return _edges;
  }

  public Integer getFrequency() {
    return _freq;
  }
}
