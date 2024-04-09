package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;

public class ArrayTypeToken extends AbstractToken {

  private final org.eclipse.jdt.core.dom.Type _elementType;

  public ArrayTypeToken(ArrayType node) {
    super(node);
    _name = node.toString();
    _elementType = node.getElementType();
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) {
    if (origin instanceof ArrayCreation) {
      ArrayInitializer initializer = ((ArrayCreation) origin).getInitializer();
      ArrayCreation target = _ast.newArrayCreation();
      List dimensions = ((ArrayCreation) origin).dimensions();
      target.setType(_ast.newArrayType(_elementType));
      target.dimensions().addAll(dimensions);
      return new Pair(target, 1.0);
    }
    throw new IllegalArgumentException();
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ArrayTypeToken) {
      return _name.equals(((ArrayTypeToken) obj)._name);
    }
    return false;
  }


}
