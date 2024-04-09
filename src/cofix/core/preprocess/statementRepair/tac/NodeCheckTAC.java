package cofix.core.preprocess.statementRepair.tac;

import java.util.ArrayList;
import java.util.List;
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

public class NodeCheckTAC extends NodeProcessor {

  private Block block;
  private  ASTNode node;
  private  NodeChildren childrenProcessor = new NodeChildren();

  @Override
  public void processChildren(ASTNode node) {
    if(node==null)return;
    if (!TACtable.containsKey(node.getParent())) {
      process(node);
    }
  }

  public NodeCheckTAC(Block block) {
    this.block = block;
  }

  public NodeCheckTAC(ASTNode block) {
    this.node = block;
  }

  public void processTAC() {
    if(block!=null){
      for(Object statement :block.statements())
        process((Statement)statement);
    }else process(node);


  }

  @Override
  public void processSelf(ASTNode node) {
    StringBuilder sb = new StringBuilder("T:= ");
    childrenProcessor.process(node);
    List<ASTNode> children = childrenProcessor.nodeChildren.get(node);
    if(children.size()==1 && children.contains(null))return;
    List<ASTNode> nodes = new ArrayList<>();
    nodes.add(node);
    nodes.addAll(children);
    if (node instanceof MethodInvocation && children.size() == 1) {
      sb.append(((MethodInvocation) node).getName().toString()).append("()");
      TACList.add(new TAC(sb.toString(), nodes));
      return;
    }
    for (ASTNode child : children) {
      if(isLeafNode(child)){
        sb.append(child.toString()).append(",");
      }else{
        sb.append("T,");
        process(child);
      }}
//      if (!isLeafNode(child)) {
//        process(child);
//        sb.append("T,");
//      } else {
//        sb.append(child.toString()).append(",");
//      }
//    }
    TACList.add(new TAC(sb.toString(), nodes));
    TACtable.put(node, sb.toString());
  }

  @Override
  public void processLeaf(ASTNode node) {
    //todo
   // this.setLeafNode(true);
//    StringBuilder sb = new StringBuilder("T:= ");
//    if(!isLeafNode(node) ){
//      sb.append(node.toString());
//    }
   // System.out.println("here");
  }

//  public boolean isLeafNode(ASTNode node) {
//    if (node instanceof NumberLiteral || node instanceof StringLiteral ||
//        node instanceof BooleanLiteral || node instanceof CharacterLiteral
//        || node instanceof NullLiteral || node instanceof SimpleName ||
//        node instanceof QualifiedName || node instanceof ArrayType || node instanceof SimpleType||node instanceof PrimitiveType) {
//      return true;
//    } else {
//
//      return false;
//    }
//  }

  public  boolean isLeafNode(ASTNode node) {
    if (node instanceof ExpressionStatement) {
      return false;
    } else if (node instanceof IfStatement) {
      return false;
    } else if (node instanceof ForStatement) {
      return false;
    } else if (node instanceof Block) {
      return false;
    } else if (node instanceof BreakStatement) {
      return false;
    } else if (node instanceof ContinueStatement) {
      return false;
    } else if (node instanceof DoStatement) {
      return false;
    } else if (node instanceof EnhancedForStatement) {
      return false;
    } else if (node instanceof ReturnStatement) {
      return false;
    } else if (node instanceof ThrowStatement) {
      return false;
    } else if (node instanceof LabeledStatement) {
      return false;
    } else if (node instanceof WhileStatement) {
      return false;
    } else if (node instanceof VariableDeclarationFragment) {
      return false;
    } else if (node instanceof VariableDeclarationStatement) {
      return false;
    } else if (node instanceof SwitchStatement) {
      return false;
    } else if (node instanceof TryStatement) {
      return false;
    } else if (node instanceof InfixExpression) {
      return false;
    } else if (node instanceof ArrayAccess) {
      return false;
    } else if (node instanceof ConditionalExpression) {
      return false;
    } else if (node instanceof MethodInvocation) {
      return false;
    } else if (node instanceof ParenthesizedExpression) {
      return false;
    } else if (node instanceof CastExpression) {
      return false;
    } else if (node instanceof PostfixExpression) {
      return false;
    } else if (node instanceof PrefixExpression) {
      return false;
    } else if (node instanceof SuperMethodInvocation) {
      return false;
    } else if (node instanceof ClassInstanceCreation) {
      return false;
    } else if (node instanceof ArrayCreation) {
      return false;
    } else if (node instanceof ArrayInitializer) {
      return false;
    }else if (node instanceof Assignment){
      return false;
    }else if (node instanceof FieldAccess){
      return false;
    }else if (node instanceof InstanceofExpression){
      return false;
    }else if(node instanceof SuperFieldAccess){
      return false;
    }else if(node instanceof ThisExpression){
      return false;
    }else {
      return true;
    }

  }
}
