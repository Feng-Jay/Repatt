package cofix.core.preprocess.token;

import cofix.common.util.DuoMap;
import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NullLiteral;

public class InfixExpressionOperatorToken extends AbstractToken {

  private static List<HashSet> _groupedOperators;

  static {
    _groupedOperators = new ArrayList<>();
    _groupedOperators.add(new HashSet<Operator>() {{
      add(Operator.PLUS);
      add(Operator.MINUS);
      add(Operator.TIMES);
      add(Operator.DIVIDE);
      add(Operator.REMAINDER);
    }});
    _groupedOperators.add(new HashSet<Operator>() {{
      add(Operator.LEFT_SHIFT);
      add(Operator.RIGHT_SHIFT_SIGNED);
      add(Operator.RIGHT_SHIFT_UNSIGNED);
    }});
    _groupedOperators.add(new HashSet<Operator>() {{
      add(Operator.LESS);
      add(Operator.GREATER);
      add(Operator.LESS_EQUALS);
      add(Operator.GREATER_EQUALS);
    }});
    _groupedOperators.add(new HashSet<Operator>() {{
      add(Operator.EQUALS);
      add(Operator.NOT_EQUALS);
    }});

    _groupedOperators.add(new HashSet<Operator>() {{
      add(Operator.AND);
      add(Operator.OR);
    }});

  }

  public InfixExpressionOperatorToken(InfixExpression infixExpression) {
    super(infixExpression);
    _name = infixExpression.getOperator().toString();
    _type = "InfixExpressionOperator";
    _ASTType = Type.InfixExpressionOperator;
  }

  public InfixExpressionOperatorToken(String newOp) {
    _name = newOp;
    _type = "InfixExpressionOperator";
    _ASTType = Type.InfixExpressionOperator;
  }

  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws WrongPatchException {
    if (origin instanceof NullLiteral) {
      return new Pair(_ast.newNullLiteral(), 1.0);
    }
    double priority = 1.0;
    InfixExpression target = _ast.newInfixExpression();
    target.setLeftOperand(
        (Expression) ASTNode.copySubtree(_ast, ((InfixExpression) origin).getLeftOperand()));
    target.setRightOperand(
        (Expression) ASTNode.copySubtree(_ast, ((InfixExpression) origin).getRightOperand()));
    InfixExpression.Operator op = Operator.toOperator(_name);
    InfixExpression.Operator originOp = ((InfixExpression) origin).getOperator();
    for (HashSet<Operator> group : _groupedOperators) {
      if (group.contains(op) && group.contains(originOp)) {
        target.setOperator(op);
        return new Pair(target, priority);
      }
    }
    throw new WrongPatchException();
  }

  @Override
  public List<AbstractToken> makeMutations() {
    Operator op = Operator.toOperator(_name);
    for (HashSet<Operator> group : _groupedOperators) {
      if (group.contains(op)) {
        List<AbstractToken> mutations = new ArrayList<>();
        for (Operator newOp : group) {
          if (newOp != op) {
            mutations.add(new InfixExpressionOperatorToken(newOp.toString()));
          }
        }
        return mutations;
      }
    }
    return null;
  }


}
