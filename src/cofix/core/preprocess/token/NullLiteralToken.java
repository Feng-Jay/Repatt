package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NullLiteral;

public class NullLiteralToken extends AbstractToken {

  public NullLiteralToken(NullLiteral nullLiteral) {
    _name = "null";
    _type = "null";
    _ASTType = Type.NullLiteral;
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws WrongPatchException {
    if (origin.getLocationInParent() != null) {
      String id = origin.getLocationInParent().getId();
      if (id.equals("leftHandSide") || id.equals("name") || id.equals("expression")) {
        throw new WrongPatchException();
      }
    }
    return new Pair(_ast.newNullLiteral(), 1.0);
  }

  @Override
  public boolean isCompatibleWith(AbstractToken token, Set<String> avaVars) {
    if (token instanceof SimpleNameToken) {
      return true;
    }
    return false;
  }


  @Override
  public boolean isLeafNode() {
    return true;
  }
}
