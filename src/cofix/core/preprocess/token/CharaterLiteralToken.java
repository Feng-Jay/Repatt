package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CharacterLiteral;

public class CharaterLiteralToken extends AbstractToken {

  public CharaterLiteralToken(CharacterLiteral characterLiteral) {
    super(characterLiteral);
    _name = characterLiteral.getEscapedValue();
    _type = "char";
    _ASTType = Type.CharacterLiteral;
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws WrongPatchException {
    if (!(origin instanceof CharacterLiteral)) {
      throw new WrongPatchException();
    }
    CharacterLiteral literal = _ast.newCharacterLiteral();
    literal.setCharValue(_name.charAt(0));
    return new Pair(literal, 1.0);
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
