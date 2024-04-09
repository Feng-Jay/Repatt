package cofix.core.preprocess.pattern;

import org.eclipse.jdt.core.dom.ASTNode;

public class Variable extends ExpArg {
  private final String _name;
  private final String _type;

  public Variable(String name, String type, ASTNode node) {
    super(name, type, node);
    _name = name;
    _type = type;
  }

  public String getName() {
    return _name;
  }

  public String getType() {
    return _type;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Variable)) {
      return false;
    }
    if (!_name.equals(((Variable) obj).getName())) {
      return false;
    }

    return true;
    // return _type.equals(((Variable) obj).getType());
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + _name.hashCode();
    // result = 31 * result + _type.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return _name;
  }
}
