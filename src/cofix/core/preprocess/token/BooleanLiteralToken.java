package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;

public class BooleanLiteralToken extends AbstractToken{

  public BooleanLiteralToken(BooleanLiteral booleanLiteral) {
    super(booleanLiteral);
    _name = booleanLiteral.toString();
    _type = "BooleanLiteral";
    _ASTType = Type.BooleanLiteral;
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars){
    Boolean value = Objects.equals(_name , "true") ?Boolean.TRUE:Boolean.FALSE;
    return new Pair(_ast.newBooleanLiteral(value),1.0);
  }

  @Override
  public boolean isLeafNode() {
    return true;
  }
}
