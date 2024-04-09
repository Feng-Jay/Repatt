package cofix.core.modification;

import cofix.common.util.Pair;
import cofix.core.modification.ArrayCreationModifyEvent.modifyType;
import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.IdentifierToken;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public abstract class ModificationEvent {

  protected AST _ast = AST.newAST(AST.JLS8);

  protected SimpleName fakeSimpleName = _ast.newSimpleName("fake");
  private modiType _type;

  public static boolean hasArguments(ASTNode node) {
    return node instanceof MethodInvocation
        || node instanceof SuperMethodInvocation
        || node instanceof ClassInstanceCreation
        || node instanceof ConstructorInvocation
        || node instanceof SuperConstructorInvocation;
  }

  public static List getArgumentsList(ASTNode root) {
    List paras = null;
    if (root instanceof MethodInvocation) {
      paras = ((MethodInvocation) root).arguments();
    } else if (root instanceof SuperMethodInvocation) {
      paras = ((SuperMethodInvocation) root).arguments();
    } else if (root instanceof ConstructorInvocation) {
      paras = ((ConstructorInvocation) root).arguments();
    } else if (root instanceof ClassInstanceCreation) {
      paras = ((ClassInstanceCreation) root).arguments();
    } else if (root instanceof SuperConstructorInvocation) {
      paras = ((SuperConstructorInvocation) root).arguments();
    }
    return paras;
  }

  public static EventList recordReplacementList(
      List<Pair<Integer, Pair<ASTNode, AbstractToken>>> replacementList) {
    EventList eventList = new EventList();
    for (Pair<Integer, Pair<ASTNode, AbstractToken>> pair : replacementList) {
      ASTNode sourceNode = pair.getSecond().getFirst();
      AbstractToken token = pair.getSecond().getSecond();

      if (token instanceof IdentifierToken) {
        continue;
      }

      if (sourceNode instanceof QualifiedName || sourceNode instanceof SimpleName
          || sourceNode instanceof StringLiteral || sourceNode instanceof CharacterLiteral
          || sourceNode instanceof NumberLiteral || sourceNode instanceof BooleanLiteral) {
        if (sourceNode.getLocationInParent().getId().equals("name")) {
          if (sourceNode.getLocationInParent().getNodeClass() == FieldAccess.class) {
            eventList.add(new FieldAccessModiEvent(token));
          } else if ((sourceNode.getLocationInParent().getNodeClass() == MethodInvocation.class)) {
            eventList.add(new MethodReplaceEvent(token));
          }
          // sourceNode is the name of a MethodInvocation
        } else if (sourceNode.getLocationInParent().getId().equals("arguments")) {
          // sourceNode is the arguments of a MethodInvocation
          ASTNode MI = sourceNode.getParent();
          while (!(hasArguments(MI))) {
            MI = MI.getParent();
          }
          List paras = getArgumentsList(MI);
          eventList.add(new ParamReplaceEvent(paras.indexOf(sourceNode), token));
        } else if (sourceNode.getLocationInParent().getId().equals("dimensions")) {
          // sourceNode is the dimensions of a ArrayCreation
          eventList.add(new ArrayCreationModifyEvent(token, modifyType.ModifyDimension));
        } else if (sourceNode.getLocationInParent().getId().equals("expression")) {
          eventList.add(new ExpressionReplaceEvent(sourceNode.getParent().getClass(), token));
        }
      } else if (sourceNode instanceof InfixExpression) {
        // sourceNode is an InfixExpression
        eventList.add(new InfixExprModifyEvent(sourceNode, token));
      }
    }
    return eventList;
  }

  public int getPriority() {
    if (this instanceof ParamReplaceEvent || this instanceof FieldAccessModiEvent) {
      return 1;
    } else if (this instanceof ParamInsertEvent) {
      return 2;
    } else if (this instanceof ParamDeleteEvent) {
      return 3;
    } else if (this instanceof MethodReplaceEvent) {
      return 4;
    } else if (this instanceof InfixExprModifyEvent) {
      return 5;
    }
    return 0;
  }

  public abstract boolean tryApply(
      ASTNode originalRoot,
      BuggyFile buggyFile,
      Set<String> availableVars,
      List<Modification> modifications)
      throws Exception;

  public enum modiType {
    paramReplace,
    paramInsert,
    paramDelete,
    methodReplace,
    infixExprModify
  }
}
