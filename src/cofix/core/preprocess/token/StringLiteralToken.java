package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StringLiteral;

public class StringLiteralToken extends AbstractToken {

  public StringLiteralToken(StringLiteral stringLiteral) {
    super(stringLiteral);
    _name = stringLiteral.getLiteralValue();
    _type = "String";
    _ASTType = Type.StringLiteral;
  }

  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws WrongPatchException {
    if (!(origin instanceof StringLiteral)) {
      throw new WrongPatchException();
    }
    StringLiteral target = _ast.newStringLiteral();
    target.setLiteralValue(_name);
    return new Pair(target, 1.0);
  }

  @Override
  public boolean isLeafNode() {
    return true;
  }

  @Override
  public boolean isCompatibleWith(AbstractToken token, Set<String> avaVars) {
    return token instanceof CharaterLiteralToken || token instanceof StringLiteralToken
        || token instanceof NullLiteralToken || (token instanceof ThisExpressionToken
        && token.getType().equals(_type));
  }
}
