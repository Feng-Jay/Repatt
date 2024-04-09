package cofix.core.preprocess.statementRepair;

import static cofix.core.preprocess.statementRepair.FragmentProcessor.InsertType.insertCondition;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import cofix.core.modification.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.search.CodeSearch;
import cofix.core.preprocess.Patch;
import cofix.core.preprocess.Patch.PatchType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

public class FragmentMutate {

  Pair<String, Integer> loc;
  BuggyFile buggyFile;
  Set<Patch> result = new HashSet<>();
  ASTNode rear;
  AST ast;
  Modification modi;
  Map<String, Type> varMap;


  public FragmentMutate(BuggyFile buggyFile, Pair<String, Integer> loc, ASTNode rear) {
    this.loc = loc;
    this.buggyFile = buggyFile;
    modi = new Modification(buggyFile);
    this.rear = rear;
    ast = rear.getAST();
    varMap = NodeUtils.getUsableVarTypes(buggyFile.getFilePath(), loc.getSecond());

  }

  public Set<Patch> mutateFix() {
   // removeCondition();
    insertIfStmt();
    insertCondition();
    return result;
  }


//  private void removeCondition() {
//    if (rear instanceof IfStatement) {
//      if (((IfStatement) rear).getExpression() instanceof InfixExpression) {
//        InfixExpression origin = (InfixExpression) ((IfStatement) rear).getExpression();
//        Expression conditionL = changeCondition(origin, 0);
//        Expression conditionR = changeCondition(origin, 1);
//        modi.replace(rear, changeStatement((IfStatement) rear, conditionL));
//        result.add(new Patch(modi.buildPatch(), 1,buggyFile, PatchType.stmt_mutate,null));
//        modi.replace(rear, changeStatement((IfStatement) rear, conditionR));
//        result.add(new Patch(modi.buildPatch(), 1, buggyFile, PatchType.stmt_mutate,null));
//      }
//    }
//  }

  private Map<String, Type> getPointerVar(Map<String, Type> map) {
    Map<String, Type> pointer = new HashMap<>();
    for (Entry<String, Type> entry : map.entrySet()) {
      if (!(entry.getValue() instanceof PrimitiveType || entry.getValue() instanceof ArrayType)) {
        pointer.put(entry.getKey(), entry.getValue());

      }
    }
    return pointer;
  }

  private void insertCondition() {
    // not equal
    Map<String,Type>map=getPointerVar(varMap);
    for (String x : map.keySet()) {
      SimpleName name = ast.newSimpleName(x);
      InfixExpression conditionNQ = (InfixExpression) getCondition(Operator.NOT_EQUALS, name);
      modi.insertBefore(rear, conditionNQ, insertCondition);
     result.add(new Patch(modi.buildPatch(), 1,buggyFile, PatchType.stmt_mutate,null));

      //equal
      InfixExpression conditionQ = (InfixExpression) getCondition(Operator.EQUALS, name);
      modi.insertBefore(rear, conditionQ, insertCondition);
     result.add(new Patch(modi.buildPatch(), 1,buggyFile, PatchType.stmt_mutate,null));
    }
  }

  private void insertIfStmt() {
//设置前提
    boolean flag = true;
    CompilationUnit cu = buggyFile.getCompilationUnit();
    CodeSearch codeSearch = new CodeSearch(cu, loc.getSecond(), 1);
    CodeBlock codeBlock = new CodeBlock(buggyFile.getFilePath(), cu, codeSearch.getASTNodes());
    if (codeBlock.getParsedNode().size() >= 1) {
      ASTNode node = codeBlock.getParsedNode().get(0).getOriginalAST();
      if (node instanceof ReturnStatement) {
        flag = false;
      }
    }
    if(flag){
    ReturnStatement defaultBody = getDefaultBody();
    Set<ReturnStatement> specifics = getSpecificBody();
    Map<String,Type>map=getPointerVar(varMap);
    for (Entry<String, Type> x : map.entrySet()) {
      SimpleName name = ast.newSimpleName(x.getKey());
      InfixExpression conditionQ = (InfixExpression) getCondition(Operator.EQUALS, name);
      modi.insertBody(conditionQ, defaultBody, rear);
      result.add(new Patch(modi.buildPatch(), 1,buggyFile, PatchType.stmt_mutate,null));
      if (specifics.size() != 0) {
        for (ReturnStatement specificBody : specifics) {
          modi.insertBody(conditionQ, specificBody, rear);
          result.add(new Patch(modi.buildPatch(), 1,buggyFile, PatchType.stmt_mutate,null));
        }
      }
    }}
  }


  private Set<ReturnStatement> getSpecificBody() {
    Set<ReturnStatement> ans = new HashSet<>();
    ASTNode method = rear;
    while (!(method instanceof MethodDeclaration)) {
      method = method.getParent();
    }
    Type type = ((MethodDeclaration) method).getReturnType2();
    if (type != null) {
      for (Entry<String, Type> x : varMap.entrySet()) {
        if (x.getValue().toString().equals(type.toString())) {//ele和return一致
          ReturnStatement body = ast.newReturnStatement();
          body.setExpression(ast.newSimpleName(x.getKey()));
          ans.add(body);
        }
      }
    }

    return ans;
  }

  private ReturnStatement getDefaultBody() {
    ASTNode method = rear;
    while (!(method instanceof MethodDeclaration)) {
      method = method.getParent();
    }

    Type type = ((MethodDeclaration) method).getReturnType2();
    ReturnStatement defaultBody = ast.newReturnStatement();

    if (type instanceof PrimitiveType) {
      PrimitiveType.Code code = ((PrimitiveType) type).getPrimitiveTypeCode();
      if (code.toString().equals("boolean")) {
        BooleanLiteral boolBody = ast.newBooleanLiteral(false);
        defaultBody.setExpression(boolBody);
      }
    } else {
      NullLiteral nullBody = ast.newNullLiteral();
      defaultBody.setExpression(nullBody);
    }
    return defaultBody;
  }


  private Expression getCondition(Operator op, SimpleName name) {
    AST ast = name.getAST();
    InfixExpression expression = ast.newInfixExpression();
    expression.setOperator(op);
    expression.setLeftOperand((Expression) ASTNode.copySubtree(ast, name));
    expression.setRightOperand(ast.newNullLiteral());
    return expression;
  }

  private IfStatement changeStatement(IfStatement origin, Expression condition) {
    AST ast = origin.getAST();
    IfStatement target = (IfStatement) ASTNode.copySubtree(ast, origin);
    Expression expression = (Expression) ASTNode.copySubtree(ast, condition);
    if (expression != null) {
      target.setExpression(expression);
    }
    return target;
  }

  private Expression changeCondition(InfixExpression origin, int lr) {
    if (origin.getOperator().toString().equals("&&") || origin.getOperator().toString()
        .equals("||")) {
      Expression target = null;
      switch (lr) {
        case 0:
          target = origin.getLeftOperand();
          break;
        case 1:
          target = origin.getRightOperand();
          break;
      }
      return target;
    }
    return null;
  }

}
