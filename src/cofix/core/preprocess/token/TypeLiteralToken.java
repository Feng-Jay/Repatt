package cofix.core.preprocess.token;

import static org.eclipse.jdt.core.dom.ASTNode.*;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeLiteral;

public class TypeLiteralToken extends AbstractToken {

  private org.eclipse.jdt.core.dom.Type _type;

  public TypeLiteralToken(TypeLiteral token) {
    super(token);
    _type = token.getType();
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws Exception {
    TypeLiteral target = _ast.newTypeLiteral();
    target.setType((org.eclipse.jdt.core.dom.Type) copySubtree(_ast, _type));
    return new Pair(target, 1.0);
  }
}
