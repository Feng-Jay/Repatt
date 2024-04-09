package cofix.core.preprocess.statementRepair;

import cofix.common.util.DuoMap;
import cofix.core.preprocess.Identifier;
import cofix.core.preprocess.pattern.Fragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
public class ExprProcessor {

  private final DuoMap<Integer, Fragment> _expressionsId = new DuoMap<>();
  private final DuoMap<Integer, String> _expressionsNameId = new DuoMap<>();
  private final Identifier _expressionsIdentifier = new Identifier(_expressionsId,
      _expressionsNameId);

  public void collectCu(ASTNode node) {
    ExpressionVisitor expressionVisitor = new ExpressionVisitor();
    node.accept(expressionVisitor);
  }

  public Identifier  getIdentifier() {
    return _expressionsIdentifier;
  }

  public DuoMap<Integer, Fragment>  getIdMap() {
    return _expressionsId;
  }

  public DuoMap<Integer, String> getNameMap() {
    return _expressionsNameId;
  }

  public List<String> getNameList() {
    List<String> ans = new ArrayList<>();
    for (Entry<Integer, String> entry : _expressionsNameId.getKv().entrySet()) {
      ans.add(entry.getValue());
    }
    return ans;
  }

  public List<ASTNode> getNodeList() {
    List<ASTNode> ans = new ArrayList<>();
    for (Entry<Integer, Fragment> node : _expressionsId.getKv().entrySet()) {
      ans.add(node.getValue().getNodeType());
    }
    return ans;
  }

  public void clear() {
    _expressionsId.clear();
    _expressionsNameId.clear();
  }

  public class ExpressionVisitor extends ASTVisitor {

    public boolean visit(ExpressionStatement node) {
      process(node);
      return false;
    }

    public boolean visit(IfStatement node) {
      process(node);
      return false;
    }

    public boolean visit(ForStatement node) {
      process(node);
      return false;
    }

    public boolean visit(Block node) {
      process(node);
      return false;
    }

    public boolean visit(BreakStatement node) {
      process(node);
      return false;
    }

    public boolean visit(ContinueStatement node) {
      process(node);
      return false;
    }

    public boolean visit(DoStatement node) {
      process(node);
      return false;
    }

    public boolean visit(EnhancedForStatement node) {
      process(node);
      return false;
    }

    public boolean visit(ReturnStatement node) {
      process(node);
      return false;
    }

    public boolean visit(ThrowStatement node) {
      process(node);
      return false;
    }

    public boolean visit(LabeledStatement node) {
      process(node);
      return false;
    }

    public boolean visit(WhileStatement node) {
      process(node);
      return false;
    }

    public boolean visit(VariableDeclarationStatement node) {
      process(node);
      return false;
    }

    public boolean visit(VariableDeclarationFragment node) {
      process(node);
      return false;
    }

    public boolean visit(SwitchStatement node) {
      process(node);
      return false;
    }

    public boolean visit(TryStatement node) {
      process(node);
      return false;
    }

    public boolean visit(SwitchCase node) {
      process(node);
      return false;
    }

  }

  public void process(ASTNode node) {
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
    }
    if (node == null) {
      return;
    }
    _expressionsIdentifier.setId(node, node.toString());
  }

  public void process(ExpressionStatement exprStmt) {
    Expression expression = exprStmt.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }
  }

  public void process(SwitchStatement switchStatement) {
    Expression expression = switchStatement.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }
    for (Object stmt : switchStatement.statements()) {
      process((ASTNode) stmt);
    }

  }

//  public void process(SwitchCase switchCase) {
//    Expression expression = switchCase.getExpression();
//    if (expression != null) {
//      _expressionsIdentifier.setId(expression, expression.toString());
//      process(expression);
//    }
//  }


  public void process(IfStatement ifStatement) {
    Expression expression = ifStatement.getExpression();
    if (expression != null) {
      process(expression);
      _expressionsIdentifier.setId(expression, expression.toString());
    }
    process(ifStatement.getThenStatement());
    if (ifStatement.getElseStatement() != null) {
      process(ifStatement.getElseStatement());
    }
  }

  public void process(ForStatement forStatement) {

    List initial = forStatement.initializers();
    if (initial.size() != 0) {
      for (Object init : initial) {
        process((ASTNode) init);
      }
    }
    Expression expression = forStatement.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }

    List updater = forStatement.updaters();
    if (updater.size() != 0) {
      for (Object update : updater) {
        process((ASTNode) update);
      }
    }
    process(forStatement.getBody());
  }

  public void process(EnhancedForStatement enhancedForStatement) {
    process(enhancedForStatement.getParameter());
    Expression expression = enhancedForStatement.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }
    process(enhancedForStatement.getBody());

  }

  public void process(Block block) {

    List stmt = block.statements();
    for (Object st : stmt) {
      process((ASTNode) st);
    }

  }


  public void process(BreakStatement breakStatement) {

    SimpleName label = breakStatement.getLabel();
    if (label != null) {
      process(label);
    }

  }

  public void process(ContinueStatement continueStatement) {

    SimpleName label = continueStatement.getLabel();
    if (label != null) {
      process(label);
    }

  }

  public void process(ReturnStatement returnStatement) {
    Expression expression = returnStatement.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }
  }

  public void process(DoStatement doStatement) {

    process(doStatement.getBody());
    Expression expression = doStatement.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }

  }

  public void process(ThrowStatement throwStatement) {
    Expression expression = throwStatement.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(throwStatement.getExpression());
    }
  }

  public void process(LabeledStatement labeledStatement) {
    SimpleName label = labeledStatement.getLabel();
    if (label != null) {
      process(label);
    }
    process(labeledStatement.getBody());

  }

  public void process(WhileStatement whileStatement) {
    Expression expression = whileStatement.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }
    process(whileStatement.getBody());

  }

  //todo
  public void process(VariableDeclarationStatement variableDeclarationStmt) {

   // List modifiers = variableDeclarationStmt.modifiers();
    variableDeclarationStmt.getType();
    List fragments = variableDeclarationStmt.fragments();
    if (fragments.size() != 0) {
      for (Object fragment : fragments) {
        process((VariableDeclarationFragment) fragment);
      }
    }

  }


  public void process(VariableDeclarationFragment variableDeclarationFragment) {
    Expression initializer = variableDeclarationFragment.getInitializer();
    if (initializer != null) {
      process(initializer);
      _expressionsIdentifier.setId(initializer, initializer.toString());
    }
  }


  public void process(TryStatement tryStatement) {
    process(tryStatement.getBody());
    if (tryStatement.getFinally() != null) {
      process(tryStatement.getFinally());
    }
  }

  public void process(CastExpression castExpr) {
    Expression expression = castExpr.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }

  }


  public void process(ClassInstanceCreation classInstanceCreation) {

    Expression expression = classInstanceCreation.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }
    List args = classInstanceCreation.arguments();
    if (args.size() != 0) {
      for (Object arg : args) {
        process((Expression) arg);
      }

    }
//    ASTNode anonymousClassDecl = classInstanceCreation.getAnonymousClassDeclaration();
//    if (anonymousClassDecl != null) {
//      process(anonymousClassDecl);
//    }

  }

  public void process(InfixExpression infixExpr) {
    Expression left = infixExpr.getLeftOperand();
    Expression right = infixExpr.getRightOperand();
    if (left != null) {
      process(left);
      _expressionsIdentifier.setId(left, left.toString());
    }
    if (right != null) {
      process(right);
      _expressionsIdentifier.setId(right,
          right.toString());
    }
  }

  public void process(ArrayAccess arrayAccess) {
    Expression array = arrayAccess.getArray();
    Expression index = arrayAccess.getIndex();
    if (array != null) {
      process(array);
      _expressionsIdentifier.setId(array, array.toString());
    }
    if (index != null) {
      process(index);
      _expressionsIdentifier.setId(index, index.toString());
    }
  }

  public void process(ConditionalExpression conditionalExpression) {
    // max=(a>b)?a:b;
    Expression expression = conditionalExpression.getExpression();
    Expression thenExpression = conditionalExpression.getThenExpression();
    Expression elseExpression = conditionalExpression.getElseExpression();
    if (expression != null) {
      process(expression);
      _expressionsIdentifier.setId(expression,
          expression.toString());
    }
    if (thenExpression != null) {
      process(thenExpression);
      _expressionsIdentifier.setId(thenExpression,
          thenExpression.toString());
    }
    if (elseExpression != null) {
      process(elseExpression);
      _expressionsIdentifier.setId(elseExpression,
          elseExpression.toString());
    }

  }


  public void process(MethodInvocation methodInvocation) {
    Expression expression = methodInvocation.getExpression();
    if (expression != null) {
      if(!isToken(expression))
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }
    Expression name = methodInvocation.getName();
    _expressionsIdentifier.setId(name, name.toString());
    process(name);
    List arguments = methodInvocation.arguments();
    if (arguments.size() != 0) {
      for (Object arg : methodInvocation.arguments()) {
        if (arg instanceof Expression) {
          process((Expression) arg);
          if(!isToken((Expression) arg))
          _expressionsIdentifier.setId((Expression) arg, arg.toString());
        }
      }
    }
  }

  public void process(ParenthesizedExpression parenthesizedExpression) {
    Expression expression = parenthesizedExpression.getExpression();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }

  }

  public void process(PostfixExpression postfixExpression) {
    Expression expression = postfixExpression.getOperand();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(expression);
    }

  }

  public void process(PrefixExpression prefixExpression) {

    Expression expression = prefixExpression.getOperand();
    if (expression != null) {
      _expressionsIdentifier.setId(expression, expression.toString());
      process(prefixExpression.getOperand());
    }
  }


  public void process(SuperMethodInvocation superMethodInvocation) {
    List arguments = superMethodInvocation.arguments();
    if (arguments.size() != 0) {
      for (Object arg : superMethodInvocation.arguments()) {
        if (arg instanceof Expression) {
          process((Expression) arg);
          _expressionsIdentifier.setId((Expression) arg, arg.toString());
        }
      }

    }

  }

  public void process(ArrayCreation arrayCreation) {
    List dimension = arrayCreation.dimensions();
    if (dimension.size() != 0) {
      for (Object dim : dimension) {
        if (dim instanceof Expression) {
          process((Expression) dim);
          _expressionsIdentifier.setId((Expression) dim, dim.toString());
        }
      }
    }
    ArrayInitializer initializer = arrayCreation.getInitializer();
    if (initializer != null) {
      process(arrayCreation.getInitializer());
      _expressionsIdentifier.setId(arrayCreation, arrayCreation.toString());
    }

  }

  public void process(ArrayInitializer arrayInitial) {
    List args = arrayInitial.expressions();
    if (args.size() != 0) {
      for (Object arg : args) {
        if (arg instanceof Expression) {
          process((Expression) arg);
          _expressionsIdentifier.setId((Expression) arg, arg.toString());
        }
      }

    }
  }



//public void process(ExpressionStatement exprStmt) {
//
//  Expression expression = exprStmt.getExpression();
//  if (expression != null) {
//    nodeProcess(expression);
//  }
//
//}
//
//  public void process(SwitchStatement switchStatement) {
//
//    Expression expression = switchStatement.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//    for (Object stmt : switchStatement.statements()) {
//      nodeProcess((ASTNode) stmt);
//    }
//
//
//  }
//
//  public void process(IfStatement ifStatement) {
//
//    Expression expression = ifStatement.getExpression();
//    nodeProcess(expression);
//    Statement thenStatement = ifStatement.getThenStatement();
//    Statement elseStatement = ifStatement.getElseStatement();
//    nodeProcess(thenStatement);
//
//    if (ifStatement.getElseStatement() != null) {
//      nodeProcess(elseStatement);
//    }
//
//  }
//
//  public void iteratorProcess(List list){
//
//    if(list.size()!=0){
//      for(Object e:list){
//        String string = ((ASTNode) e).toString();
//        if(isLeafNode((ASTNode) e)){
//          if(!isToken((ASTNode) e)){
//            _expressionsIdentifier.setId((ASTNode) e,string);
//          }
//        }else{
//          _expressionsIdentifier.setId((ASTNode) e,string);
//          process((ASTNode) e);
//        }
//      }
//    }
//
//  }
//
//  public void nodeProcess(ASTNode node){
//
//    if(isLeafNode(node)){
//      if(!isToken(node)){
//        _expressionsIdentifier.setId(node,node.toString());
//      }
//    }else{
//      _expressionsIdentifier.setId(node,node.toString());
//      process(node);
//    }
//
//  }
//  public void process(ForStatement forStatement) {
//
//    List initial = forStatement.initializers();
//    if (initial.size() != 0) {
//      iteratorProcess(initial);
//    }
//    Expression expression = forStatement.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//    List updater = forStatement.updaters();
//    if (updater.size() != 0) {
//      iteratorProcess(updater);
//    }
//    Statement body = forStatement.getBody();
//    nodeProcess(body);
//
//  }
//
//  public void process(EnhancedForStatement enhancedForStatement) {
//
//    ASTNode parameter=enhancedForStatement.getParameter();
//    nodeProcess(parameter);
//    Expression expression = enhancedForStatement.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//    Statement body=enhancedForStatement.getBody();
//    nodeProcess(body);
//
//  }
//
//  public void process(Block block) {
//
//    List stmt = block.statements();
//    iteratorProcess(stmt);
//
//  }
//
//
//  public void process(BreakStatement breakStatement) {
//
//    SimpleName label = breakStatement.getLabel();
//    if (label != null) {
//      nodeProcess(label);
//    }
//
//  }
//
//  public void process(ContinueStatement continueStatement) {
//
//    SimpleName label = continueStatement.getLabel();
//    if (label != null) {
//      nodeProcess(label);
//    }
//
//  }
//
//  public void process(ReturnStatement returnStatement) {
//
//    Expression expression = returnStatement.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//
//  }
//
//
//  public void process(DoStatement doStatement) {
//
//    Statement body = doStatement.getBody();
//    nodeProcess(body);
//    Expression expression = doStatement.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//
//  }
//
//  public void process(ThrowStatement throwStatement) {
//
//    Expression expression = throwStatement.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//
//  }
//
//  public void process(LabeledStatement labeledStatement) {
//
//    SimpleName label = labeledStatement.getLabel();
//    if (label != null) {
//      nodeProcess(label);
//    }
//    Statement body=labeledStatement.getBody();
//    nodeProcess(body);
//
//  }
//
//  public void process(WhileStatement whileStatement) {
//
//    Expression expression = whileStatement.getExpression();
//    Statement body = whileStatement.getBody();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//    nodeProcess(body);
//
//  }
//
//
//  public void process(VariableDeclarationStatement variableDeclarationStmt) {
//
//
//    List modifiers = variableDeclarationStmt.modifiers();
//    variableDeclarationStmt.getType();
//    List fragments = variableDeclarationStmt.fragments();
//    iteratorProcess(fragments);
//
//  }
//
//
//  public void process(VariableDeclarationFragment variableDeclarationFragment) {
//
//    Expression initializer = variableDeclarationFragment.getInitializer();
//    if (initializer != null) {
//      nodeProcess(initializer);
//    }
//
//  }
//
//
//  public void process(TryStatement tryStatement) {
//
//    Statement body=tryStatement.getBody();
//    Block finalBlock=tryStatement.getFinally();
//    nodeProcess(body);
//    if (tryStatement.getFinally() != null) {
//      nodeProcess(finalBlock);
//    }
//
//  }
//
//  public void process(CastExpression castExpr) {
//
//    Expression expression = castExpr.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//
//  }
//
//
//  public void process(ClassInstanceCreation classInstanceCreation) {
//
//    Expression expression = classInstanceCreation.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//    List args = classInstanceCreation.arguments();
//    iteratorProcess(args);
//
//  }
//
//  public void process(InfixExpression infixExpr) {
//
//    Expression left = infixExpr.getLeftOperand();
//    Expression right = infixExpr.getRightOperand();
//    if (left != null) {
//      nodeProcess(left);
//    }
//    if (right != null) {
//      nodeProcess(right);
//    }
//
//  }
//
//  public void process(ArrayAccess arrayAccess) {
//
//    Expression array = arrayAccess.getArray();
//    Expression index = arrayAccess.getIndex();
//    if (array != null) {
//      nodeProcess(array);
//    }
//    if (index != null) {
//      nodeProcess(index);
//    }
//
//  }
//
//  public void process(ConditionalExpression conditionalExpression) {
//    Expression expression = conditionalExpression.getExpression();
//    Expression thenExpression = conditionalExpression.getThenExpression();
//    Expression elseExpression = conditionalExpression.getElseExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//    if (thenExpression != null) {
//      nodeProcess(thenExpression);
//    }
//    if (elseExpression != null) {
//      nodeProcess(elseExpression);
//    }
//
//  }
//
//
//  public void process(MethodInvocation methodInvocation) {
//
//    Expression expression = methodInvocation.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//    Expression name = methodInvocation.getName();
//    nodeProcess(name);
//    List arguments = methodInvocation.arguments();
//    iteratorProcess(arguments);
//
//  }
//
//  public void process(ParenthesizedExpression parenthesizedExpression) {
//
//    Expression expression = parenthesizedExpression.getExpression();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//
//  }
//
//  public void process(PostfixExpression postfixExpression) {
//
//    Expression expression = postfixExpression.getOperand();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//
//  }
//
//  public void process(PrefixExpression prefixExpression) {
//
//    Expression expression = prefixExpression.getOperand();
//    if (expression != null) {
//      nodeProcess(expression);
//    }
//
//  }
//
//
//  public void process(SuperMethodInvocation superMethodInvocation) {
//
//    List arguments = superMethodInvocation.arguments();
//    iteratorProcess(arguments);
//
//  }
//
//  public void process(ArrayCreation arrayCreation) {
//
//    List dimension = arrayCreation.dimensions();
//    iteratorProcess(dimension);
//    ArrayInitializer initializer = arrayCreation.getInitializer();
//    if (initializer != null) {
//      nodeProcess(initializer);
//    }
//
//  }
//
//  public void process(ArrayInitializer arrayInitial) {
//
//    List args = arrayInitial.expressions();
//    iteratorProcess(args);
//
//  }
//
//
//
//
  public boolean isToken(ASTNode node) {
    if (node instanceof NumberLiteral || node instanceof StringLiteral ||
        node instanceof BooleanLiteral || node instanceof CharacterLiteral
        || node instanceof NullLiteral) {
      return true;
    }
    if (node instanceof SimpleName) {
      if (isCondition(node) || isMethodName(node) || isSwitchExpression(node)) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  public boolean isMethodName(ASTNode node) {
    if (node.getParent() instanceof MethodInvocation && node.getLocationInParent().getId()
        .equals("name")) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isSwitchExpression(ASTNode node) {
    if (node.getParent() instanceof SwitchStatement && node.getLocationInParent().getId()
        .equals("expression")) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isCondition(ASTNode node) {
    if (node.getParent() instanceof IfStatement && node.getLocationInParent(). getId()
        .equals("expression")) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isLeafNode(ASTNode node) {
    if (node instanceof NumberLiteral || node instanceof StringLiteral ||
        node instanceof BooleanLiteral || node instanceof CharacterLiteral
        || node instanceof NullLiteral || node instanceof SimpleName ||
        node instanceof QualifiedName) {
      return true;
    } else {
      return false;
    }
  }
}





