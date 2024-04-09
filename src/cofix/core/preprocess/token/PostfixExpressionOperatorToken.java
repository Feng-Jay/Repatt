package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;

public class PostfixExpressionOperatorToken extends AbstractToken{

  public PostfixExpressionOperatorToken(PostfixExpression postfixExpression){
    super(postfixExpression);
    _name = postfixExpression.getOperator().toString();
    _type = "PostfixExpressionOperator";
    _ASTType = Type.PostfixExpressionOperator;
  }

  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars){
    if(origin instanceof NullLiteral){
      return new Pair(_ast.newNullLiteral(),1.0);
    }
    PostfixExpression target = _ast.newPostfixExpression();
    target.setOperand((Expression) ASTNode.copySubtree(_ast, ((PostfixExpression)origin).getOperand()));
    PostfixExpression.Operator op;
    switch (_name){
      case "++":
        op = PostfixExpression.Operator.INCREMENT;
        break;
      case "--":
        op = PostfixExpression.Operator.DECREMENT;
        break;
      default:
        throw new IllegalArgumentException();
    }
    target.setOperator(op);
    return new Pair(target,1.0);
  }

}
