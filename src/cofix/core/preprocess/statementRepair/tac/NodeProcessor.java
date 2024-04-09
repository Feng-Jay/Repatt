package cofix.core.preprocess.statementRepair.tac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public abstract class NodeProcessor {

  public abstract void processChildren(ASTNode node);

  public abstract void processSelf(ASTNode node);

  public abstract void processLeaf(ASTNode node);

  public  Map<ASTNode, List<ASTNode>> nodeChildren = new HashMap<>();
  public  Map<ASTNode, String> TACtable = new HashMap<>();// k:node v:TAC label
  public ASTNode currentNode;
  public List<TAC> TACList = new ArrayList<>();
 // private boolean isLeafNode=false;
  //static Map<Block, TAC>TAC=new HashMap<>();// k:block v:TAC lists


//  public boolean isLeafNode() {
//    return isLeafNode;
//  }
//
//  public void setLeafNode(boolean leafNode) {
//    isLeafNode = leafNode;
//  }

  public void process(ASTNode node) {

    if(node ==null)return;
    if (node instanceof ExpressionStatement) {
      process((ExpressionStatement) node);
    } else if (node instanceof IfStatement) {
      process((IfStatement) node);
    } else if (node instanceof ForStatement) {
      process((ForStatement) node);
    } else if (node instanceof Block) {
      process((Block) node);
    } else if (node instanceof BreakStatement) {
      process((BreakStatement) node);
    } else if (node instanceof ContinueStatement) {
      process((ContinueStatement) node);
    } else if (node instanceof DoStatement) {
      process((DoStatement) node);
    } else if (node instanceof EnhancedForStatement) {
      process((EnhancedForStatement) node);
    } else if (node instanceof ReturnStatement) {
      process((ReturnStatement) node);
    } else if (node instanceof ThrowStatement) {
      process((ThrowStatement) node);
    } else if (node instanceof LabeledStatement) {
      process((LabeledStatement) node);
    } else if (node instanceof WhileStatement) {
      process((WhileStatement) node);
    } else if (node instanceof VariableDeclarationFragment) {
      process((VariableDeclarationFragment) node);
    } else if (node instanceof VariableDeclarationStatement) {
      process((VariableDeclarationStatement) node);
    } else if (node instanceof SwitchStatement) {
      process((SwitchStatement) node);
    } else if (node instanceof TryStatement) {
      process((TryStatement) node);
    } else if (node instanceof InfixExpression) {
      process((InfixExpression) node);
    } else if (node instanceof ArrayAccess) {
      process((ArrayAccess) node);
    } else if (node instanceof ConditionalExpression) {
      process((ConditionalExpression) node);
    } else if (node instanceof MethodInvocation) {
      process((MethodInvocation) node);
    } else if (node instanceof ParenthesizedExpression) {
      process((ParenthesizedExpression) node);
    } else if (node instanceof CastExpression) {
      process((CastExpression) node);
    } else if (node instanceof PostfixExpression) {
      process((PostfixExpression) node);
    } else if (node instanceof PrefixExpression) {
      process((PrefixExpression) node);
    } else if (node instanceof SuperMethodInvocation) {
      process((SuperMethodInvocation) node);
    } else if (node instanceof ClassInstanceCreation) {
      process((ClassInstanceCreation) node);
    } else if (node instanceof ArrayCreation) {
      process((ArrayCreation) node);
    } else if (node instanceof ArrayInitializer) {
      process((ArrayInitializer) node);
    }else if (node instanceof Assignment){
      process((Assignment)node);
    }else if (node instanceof FieldAccess){
      process((FieldAccess)node);
    }else if (node instanceof InstanceofExpression){
      process((InstanceofExpression)node);
    }else if(node instanceof SuperFieldAccess){
      process((SuperFieldAccess) node);
    }else  if(node instanceof ThisExpression){
      process((ThisExpression) node);
    }else{
      List<Integer> ans = new LinkedList<>();
      //isLeafNode
      processLeaf(node);
    }
  }

  public void process(SuperFieldAccess superFieldAccess){
    processSelf(superFieldAccess);
    processChildren(superFieldAccess.getQualifier());
    processChildren(superFieldAccess.getName());
  }

  public void process(ThisExpression thisExpression){
    processSelf(thisExpression);
    processChildren(thisExpression.getQualifier());
  }

  public void process(FieldAccess fieldAccess){
    processSelf(fieldAccess);
   processChildren(fieldAccess.getExpression());
   processChildren(fieldAccess.getName());
  }

  public  void  process(InstanceofExpression instanceofExpression){
    processSelf(instanceofExpression);
    processChildren(instanceofExpression.getLeftOperand());
    processChildren(instanceofExpression.getRightOperand());
  }

public void process(Assignment assignment){
    processSelf(assignment);
    processChildren(assignment.getLeftHandSide());
    processChildren(assignment.getRightHandSide());
}

  public void process(EnhancedForStatement enhancedForStatement) {
    processSelf(enhancedForStatement);
    ASTNode parameter = enhancedForStatement.getParameter();
    processChildren(parameter);
    Expression expression = enhancedForStatement.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    Statement body = enhancedForStatement.getBody();
    processChildren(body);
    return;
  }

  public void process(ContinueStatement continueStatement) {
    processSelf(continueStatement);
    SimpleName label = continueStatement.getLabel();
    if (label != null) {
      processChildren(label);
    }
    return;
  }

  public void process(IfStatement ifStatement) {
    processSelf(ifStatement);
    Expression expression = ifStatement.getExpression();
    processChildren(expression);
    Statement thenStatement = ifStatement.getThenStatement();
    processChildren(thenStatement);
    Statement elseStatement = ifStatement.getElseStatement();
    if (elseStatement != null) {
      processChildren(elseStatement);
    }
    return;
  }

  public void process(BreakStatement breakStatement) {
    processSelf(breakStatement);
    List<Integer> ans = new LinkedList<>();
    SimpleName label = breakStatement.getLabel();
    if (label != null) {
      processChildren(label);
    }
    return;
  }


  public void process(SwitchStatement switchStatement) {
    processSelf(switchStatement);
    Expression expression = switchStatement.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    for (Object stmt : switchStatement.statements()) {
      processChildren((ASTNode) stmt);
    }
    return;
  }

  public void process(ReturnStatement returnStatement) {
    processSelf(returnStatement);
    List<Integer> ans = new LinkedList<>();
    Expression expression = returnStatement.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    return;
  }

  public void process(DoStatement doStatement) {
    processSelf(doStatement);
    List<Integer> ans = new LinkedList<>();
    Statement body = doStatement.getBody();
    processChildren(body);
    Expression expression = doStatement.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    return;
  }

  public void process(ExpressionStatement exprStmt) {
    processSelf(exprStmt);
    List<Integer> ans = new LinkedList<>();
    Expression expression = exprStmt.getExpression();
    if (expression != null) {

      processChildren(expression);
    }
    return;
  }

  public void process(ThrowStatement throwStatement) {
    processSelf(throwStatement);
    Expression expression = throwStatement.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    return;
  }

  public void process(LabeledStatement labeledStatement) {
    processSelf(labeledStatement);
    SimpleName label = labeledStatement.getLabel();
    if (label != null) {
      processChildren(label);
    }
    Statement body = labeledStatement.getBody();
    processChildren(body);
    return;
  }


  public void process(WhileStatement whileStatement) {
    processSelf(whileStatement);
    Expression expression = whileStatement.getExpression();
    Statement body = whileStatement.getBody();
    if (expression != null) {
      processChildren(expression);
    }
    processChildren(body);
    return;
  }

  public void process(TryStatement tryStatement) {
    processSelf(tryStatement);
    Statement body = tryStatement.getBody();
    Block finalBlock = tryStatement.getFinally();
    processChildren(body);
    if (finalBlock != null) {
      processChildren(finalBlock);
    }
    return;
  }

  public void process(CastExpression castExpr) {
    processSelf(castExpr);
    Expression expression = castExpr.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    return;
  }

  public void process(InfixExpression infixExpr) {
    processSelf(infixExpr);
    Expression left = infixExpr.getLeftOperand();
    Expression right = infixExpr.getRightOperand();
    if (left != null) {
      processChildren(left);
    }

    if (right != null) {
      processChildren(right);
    }
    return;
  }

  public void process(ArrayAccess arrayAccess) {
    processSelf(arrayAccess);
    Expression array = arrayAccess.getArray();
    Expression index = arrayAccess.getIndex();
    if (array != null) {
      processChildren(array);
    }
    if (index != null) {
      processChildren(index);
    }
    return;
  }

  public void process(ConditionalExpression conditionalExpression) {
    processSelf(conditionalExpression);
    // max=(a>b)?a:b;
    Expression expression = conditionalExpression.getExpression();
    Expression thenExpression = conditionalExpression.getThenExpression();
    Expression elseExpression = conditionalExpression.getElseExpression();
    if (expression != null) {
      processChildren(expression);
    }
    if (thenExpression != null) {
      processChildren(thenExpression);
    }
    if (elseExpression != null) {
      processChildren(elseExpression);
    }
    return;
  }

  public void process(PostfixExpression postfixExpression) {
    processSelf(postfixExpression);
    Expression expression = postfixExpression.getOperand();
    if (expression != null) {
      processChildren(expression);
    }
    return;
  }

  public void process(PrefixExpression prefixExpression) {
    processSelf(prefixExpression);
    Expression expression = prefixExpression.getOperand();
    if (expression != null) {
      processChildren(expression);
    }
    return;
  }

  public void process(SuperMethodInvocation superMethodInvocation) {
    processSelf(superMethodInvocation);
    List arguments = superMethodInvocation.arguments();
    //todo:super
    if (arguments.size() != 0) {
      for (Object arg : superMethodInvocation.arguments()) {
        processChildren((ASTNode) arg);
      }
    }
    return;
  }

  public void process(ForStatement forStatement) {
    processSelf(forStatement);
    List<Integer> ans = new LinkedList<>();
    List initial = forStatement.initializers();
    for (Object init : initial) {
      processChildren((ASTNode) init);
    }
    Expression expression = forStatement.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    List updater = forStatement.updaters();
    for (Object update : updater) {
      processChildren((ASTNode) update);
    }
    Statement body = forStatement.getBody();
    processChildren(body);
    return;
  }

  public void process(ArrayCreation arrayCreation) {
    processSelf(arrayCreation);
    List dimension = arrayCreation.dimensions();
    if (dimension.size() != 0) {
      for (Object dim : dimension) {
        processChildren((ASTNode) dim);
      }
    }
    ArrayInitializer initializer = arrayCreation.getInitializer();
    if (initializer != null) {
      processChildren(initializer);
    }
    return;
  }


  public void process(Block block) {
    processSelf(block);
    List stmt = block.statements();
    for (Object st : stmt) {
      processChildren((ASTNode) st);
    }
    return;
  }


  public void process(ArrayInitializer arrayInitial) {
    processSelf(arrayInitial);
    List args = arrayInitial.expressions();
    // if (args.size() != 0) {
    for (Object arg : args) {
      processChildren((ASTNode) arg);
    }
    //    }
    return;
  }


  public void process(VariableDeclarationStatement variableDeclarationStmt) {
    processSelf(variableDeclarationStmt);
    List<Integer> ans = new LinkedList<>();
    //todo baseType e.g. double[]
    processChildren(variableDeclarationStmt.getType());
    List fragments = variableDeclarationStmt.fragments();
    if (fragments.size() != 0) {
      for (Object fragment : fragments) {
        processChildren((VariableDeclarationFragment) fragment);
      }
    }
    return;
  }


  public void process(VariableDeclarationFragment variableDeclarationFragment) {
    processSelf(variableDeclarationFragment);
    List<Integer> ans = new LinkedList<>();
    processChildren(variableDeclarationFragment.getName());
    Expression initializer = variableDeclarationFragment.getInitializer();
    if (initializer != null) {
      processChildren(initializer);
    }
    return;
  }


  public void process(MethodInvocation methodInvocation) {
    processSelf(methodInvocation);
    List<Integer> ans = new LinkedList<>();
    Expression expression = methodInvocation.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    Expression name = methodInvocation.getName();
    processChildren(name);
    List arguments = methodInvocation.arguments();
    if (arguments.size() != 0) {
      for (Object arg : methodInvocation.arguments()) {
        processChildren((Expression) arg);
      }
    }
    return;
  }


  public void process(ParenthesizedExpression parenthesizedExpression) {
    processSelf(parenthesizedExpression);
    List<Integer> ans = new LinkedList<>();
    Expression expression = parenthesizedExpression.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    return;
  }

  public void process(ClassInstanceCreation classInstanceCreation) {
    processSelf(classInstanceCreation);
    List<Integer> ans = new LinkedList<>();
    Expression expression = classInstanceCreation.getExpression();
    if (expression != null) {
      processChildren(expression);
    }
    processChildren(classInstanceCreation.getType());
    List args = classInstanceCreation.arguments();
    if (args.size() != 0) {
      for (Object arg : args) {
        processChildren((Expression) arg);
      }
      return;
    }
  }


}
