package cofix.core.preprocess.pattern;

import org.eclipse.jdt.core.dom.ASTNode;

public class Fragment {

  private Integer fragmentTimes;
  private final ASTNode nodeType;

  public Fragment(ASTNode node) {
    fragmentTimes = 0;
    nodeType = node;
  }

  public Integer getTimes() {
    return fragmentTimes;
  }

  public ASTNode getNodeType() {
    return nodeType;
  }


  public void setTimes() {
    fragmentTimes++;
  }
}
