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
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;


public class TokenProcessorFre {
  private final DuoMap<Integer, Fragment> _tokensId = new DuoMap<>();
  private final DuoMap<Integer, String> _tokensNameId = new DuoMap<>();
  private final Identifier _tokensIdentifier =new Identifier(_tokensId,_tokensNameId);

  public void collectCu(ASTNode node) {
   TokensVisitorFre tokensVisitorFre =new TokensVisitorFre();
    node.accept(tokensVisitorFre);
  }


public Identifier getIdentifier(){return _tokensIdentifier;}

  public DuoMap<Integer, Fragment> getIdMap(){
    return  _tokensId;
  }

  public DuoMap<Integer,String> getNameMap(){
    return  _tokensNameId;
  }

  public List<String>getNameList(){
    List<String>ans = new ArrayList<>();
    for(Entry<Integer,String> entry:_tokensNameId.getKv().entrySet()){
      ans.add(entry.getValue());
    }
    return ans;
  }

  public List<ASTNode>getNodelist(){
    List<ASTNode>ans = new ArrayList<>();
    for(Entry<Integer,Fragment> entry:_tokensId.getKv().entrySet()){
      ans.add(entry.getValue().getNodeType());
    }
    return ans;
  }
  public void clear(){
    _tokensId.clear();
    _tokensNameId.clear();
  }

  public class TokensVisitorFre extends ASTVisitor {
    public boolean visit(SimpleName node) {
      process(node);
      return false;
    }

    public boolean visit(BooleanLiteral node) {
      process(node);
      return false;
    }

    public boolean visit(StringLiteral node) {
      process(node);
      return false;
    }

    public boolean visit(InfixExpression node) {
      process(node);
      return false;
    }
    public boolean visit(ArrayAccess node) {
      process(node);
      return false;
    }
    public boolean visit(ConditionalExpression node) {
      process(node);
      return false;
    }
    public boolean visit(LambdaExpression node) {
      process(node);
      return false;
    }
    public boolean visit(MethodInvocation node) {
      process(node);
      return false;
    }
    public boolean visit(ParenthesizedExpression node) {
      process(node);
      return false;
    }
    public boolean visit(CastExpression node) {
      process(node);
      return false;
    }
    public boolean visit(PostfixExpression node) {
      process(node);
      return false;
    }
    public boolean visit(PrefixExpression node) {
      process(node);
      return false;
    }
    public boolean visit(SuperFieldAccess node) {
      process(node);
      return false;
    }
    public boolean visit(SuperMethodInvocation node) {
      process(node);
      return false;
    }
    public boolean visit(ClassInstanceCreation node) {
      process(node);
      return false;
    }
    public boolean visit(ArrayCreation node) {
      process(node);
      return false;
    }
    public boolean visit(ArrayInitializer node) {
      process(node);
      return false;
    }
    public boolean visit(InstanceofExpression node) {
      process(node);
      return false;
    }
    public void process(ASTNode node) {
      if (node instanceof InfixExpression) {
         process((InfixExpression) node);
      } else if (node instanceof ArrayAccess) {
         process((ArrayAccess) node);
      } else if (node instanceof ConditionalExpression) {
         process((ConditionalExpression) node);
      } else if (node instanceof LambdaExpression) {
         process((LambdaExpression) node);
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
      } else if (node instanceof SuperFieldAccess) {
         process((SuperFieldAccess) node);
      } else if (node instanceof SuperMethodInvocation) {
         process((SuperMethodInvocation) node);
      } else if (node instanceof ClassInstanceCreation) {
         process((ClassInstanceCreation) node);
      } else if (node instanceof ArrayCreation) {
         process((ArrayCreation) node);
      } else if (node instanceof ArrayInitializer) {
         process((ArrayInitializer) node);
      } else if (node instanceof InstanceofExpression) {
         process((InstanceofExpression) node);
      }
      _tokensIdentifier.setId(node, node.toString());
    }


    public void process(CastExpression castExpr) {

      process(castExpr.getType());
      process(castExpr.getExpression());

    }

    public void process(ClassInstanceCreation classInstanceCreation) {

      Expression expression = classInstanceCreation.getExpression();
      if (expression != null) {
        process(expression);
      }
      // ?IS there should be a <TYPE>??
      process(classInstanceCreation.getType());
      List args = classInstanceCreation.arguments();
      if (args.size() != 0) {
        for (Object arg : args) {
          process((Expression) arg);
        }

      }
      ASTNode anonymousClassDecl = classInstanceCreation.getAnonymousClassDeclaration();
      if (anonymousClassDecl != null) {
        process(anonymousClassDecl);
      }

    }

    public void process(InfixExpression infixExpr) {

      process(infixExpr.getLeftOperand());
      //TODO:operator
      // ans.add(process(infixExpr.getOperator().toString()));
      process(infixExpr.getRightOperand());

    }

    public void process(ArrayAccess arrayAccess) {

      process(arrayAccess.getArray());
      process(arrayAccess.getIndex());

    }

    public void process(ConditionalExpression conditionalExpression) {
      // max=(a>b)?a:b;
      process(conditionalExpression.getExpression());
      process(conditionalExpression.getThenExpression());
      process(conditionalExpression.getElseExpression());

    }

    public void process(LambdaExpression lambdaExpression) {

      List para = lambdaExpression.parameters();
      if (para != null) {
        for (Object parameter : para) {
          process((ASTNode) parameter);
        }

      }
      process(lambdaExpression.getBody());

    }

    public void process(MethodInvocation methodInvocation) {


      Expression expression = methodInvocation.getExpression();
      //todo
//      AST ast=
//      MethodInvocation newMI=methodInvocation.getAST().newMethodInvocation();
      if (expression != null) {
        process(expression);
       // newMI.setName((SimpleName) ASTNode.copySubtree(methodInvocation.getAST(),methodInvocation.getName()));
      }
      process(methodInvocation.getName());
      List arguments = methodInvocation.arguments();
      if (arguments.size() != 0) {
        for (Object arg : methodInvocation.arguments()) {
          process((Expression) arg);
//          ASTNode newArg= new ASTNode(ASTNode.copySubtree(arg))
//          newMI.arguments().add(arg);
        }
      }

//      if(newMI.getName() != null){
//        process(newMI);
//      }
    }

    public void process(ParenthesizedExpression parenthesizedExpression) {

      process(parenthesizedExpression.getExpression());

    }

    public void process(PostfixExpression postfixExpression) {

      process(postfixExpression.getOperand());
      //ToDo:operator
      // ans.add(process(postfixExpression.getOperator()));

    }

    public void process(PrefixExpression prefixExpression) {

      //ToDo:operator
      // ans.add(process(prefixExpression.getOperator());
      process(prefixExpression.getOperand());

    }

    public void process(SuperFieldAccess superFieldAccess) {
   process(superFieldAccess.getName());

    }

    public void process(SuperMethodInvocation superMethodInvocation) {

      process(superMethodInvocation.getName());
      List arguments = superMethodInvocation.arguments();
      if (arguments.size() != 0) {
        for (Object arg : superMethodInvocation.arguments()) {
          process((Expression) arg);
        }

      }

    }

    public void process(ArrayCreation arrayCreation) {

      List dimension = arrayCreation.dimensions();
      if (dimension.size() != 0) {
        process(arrayCreation.getType().getElementType());
        for (Object dim : dimension) {
          process((Expression) dim);
        }
      }
      ArrayInitializer initializer = arrayCreation.getInitializer();
      if (initializer != null) {
        process(arrayCreation.getInitializer());
      }

    }

    public void process(ArrayInitializer arrayInitial) {

      List args = arrayInitial.expressions();
      if (args.size() != 0) {
        for (Object arg : args) {
          process((Expression) arg);
        }

      }

    }

    public void process(InstanceofExpression expr) {

      process(expr.getLeftOperand());
      process(expr.getRightOperand());

    }


  }


}
