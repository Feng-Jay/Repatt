package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;

public class PrefixExpressionOperatorToken extends AbstractToken{

  public PrefixExpressionOperatorToken(PrefixExpression prefixExpression){
    super(prefixExpression);
    _name = prefixExpression.getOperator().toString();
    _type = "PrefixExpressionOperator";
    _ASTType = Type.PrefixExpressionOperator;
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars){
    if(origin instanceof NullLiteral){
      return new Pair(_ast.newNullLiteral(),1.0);
    }
    PrefixExpression target = _ast.newPrefixExpression();
    target.setOperand((Expression) ASTNode.copySubtree(_ast, ((PrefixExpression)origin).getOperand()));
    PrefixExpression.Operator op;
    switch (_name) {
      case "++":
        op = PrefixExpression.Operator.INCREMENT;
        break;
      case "--":
        op = PrefixExpression.Operator.DECREMENT;
        break;
      case "-":
        op = Operator.MINUS;
        break;
      case "~":
        op = Operator.COMPLEMENT;
        break;
      case "!":
        op = Operator.NOT;
        break;
      case "+":
        op = Operator.PLUS;
        break;
      default:
        throw new IllegalArgumentException();
    }
    target.setOperator(op);
    return new Pair(target,1.0);
  }
}
