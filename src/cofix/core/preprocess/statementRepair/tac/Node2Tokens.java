package cofix.core.preprocess.statementRepair.tac;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * 收集一个ASTNode中的所有SimpleName
 *
 * @author yezizhi
 * @date 2022/11/29
 */
public class Node2Tokens extends NodeProcessor{

  private Set<String> tokens=new HashSet<>();

  public Set<String> getTokens() {
    return tokens;
  }

  @Override
  public void processChildren(ASTNode node) {
    process(node);
  }

  @Override
  public void processSelf(ASTNode node) {

  }

  @Override
  public void processLeaf(ASTNode node) {
if(node instanceof SimpleName){
  tokens.add(node.toString());
}
  }
}
