package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NumberLiteral;

public class NumberLiteralToken extends AbstractToken {

  public NumberLiteralToken(NumberLiteral numberLiteral) {
    super(numberLiteral);
    _name = numberLiteral.toString();
    ITypeBinding type = numberLiteral.resolveTypeBinding();
    _type = type == null ? "Failed" : type.getName();
    _ASTType = Type.NumberLiteral;
  }

  public NumberLiteralToken(NumberLiteralToken originalToken, String newType, String newName) {
    this._ASTType = Type.NumberLiteral;
    this._type = newType;
    this._name = newName;
  }

  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws WrongPatchException {
    if (!(origin instanceof NumberLiteral)) {
      throw new WrongPatchException();
    }
    return new Pair(_ast.newNumberLiteral(_name), 1.0);
  }

  @Override
  public List<AbstractToken> makeMutations() {
    List<AbstractToken> result = new ArrayList<>();
    if (_name.contains("x")) {
      return null;
    }
    result.add(new NumberLiteralToken(this, "double", _name + "D"));
    return result;
  }

  @Override
  public boolean isLeafNode() {
    return true;
  }
}
