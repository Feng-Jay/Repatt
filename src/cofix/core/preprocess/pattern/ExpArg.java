package cofix.core.preprocess.pattern;

import org.eclipse.jdt.core.dom.ASTNode;

public class ExpArg {
  private final String _oriName;
  private final String _type;
  private final ASTNode _oriAst;

  public ExpArg(String origin, String type, ASTNode node) {
    _oriName = origin;
    _type = type;
    _oriAst = node;
  }

  public String getName() {
    return _oriName;
  }

  public String getType() {
    return _type;
  }

  public ASTNode getOriNode() {
    return _oriAst;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ExpArg && ((ExpArg) obj).getName().equals(_oriName);
  }

  @Override
  public int hashCode() {
    int result = 17;
    return result * 31 + _oriName.hashCode();
  }

  @Override
  public String toString() {
    return _oriName;
  }
}
