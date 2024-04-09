package cofix.core.preprocess.statementRepair.tac;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * 得到一个节点的孩子（仅往下一层）
 *
 * @author yezizhi
 * @date 2022/11/19
 */

public class NodeChildren extends NodeProcessor {


  @Override
  public void processChildren(ASTNode node) {
//    if (nodeChildren.containsKey(currentNode)) {
      List<ASTNode> list = this.nodeChildren.get(currentNode);
      list.add(node);
      this.nodeChildren.replace(currentNode, list);
  //  }
  }

  @Override
  public void processSelf(ASTNode node) {
    this.nodeChildren.put(node, new ArrayList<>());
    currentNode=node;
  }

  @Override
  public void processLeaf(ASTNode node) {
   // nodeChildren.put(node, new ArrayList<>());
  }
}
