package cofix.core.modification;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class StructureVisitor extends ASTVisitor {

  private final Class _target;
  private final List<ASTNode> _nodes = new ArrayList<>();

  public StructureVisitor(Class targetClass) {
    _target = targetClass;
  }

  public boolean visit(MethodDeclaration node) {
    if (_target.equals(MethodDeclaration.class)) {
      _nodes.add(node);
    }
    return true;
  }

  public List<ASTNode> getNodes() {
    return _nodes;
  }

  public boolean visit(MethodInvocation node) {
    if (_target.equals(MethodInvocation.class)) {
      _nodes.add(node);
    }
    return true;
  }

  public boolean visit(InfixExpression node) {
    if (_target.equals(InfixExpression.class)) {
      _nodes.add(node);
    }
    return true;
  }

  public boolean visit(ClassInstanceCreation node) {
    if (_target.equals(ClassInstanceCreation.class)) {
      _nodes.add(node);
    }
    return true;
  }

  public boolean visit(SuperMethodInvocation node) {
    if (_target.equals(SuperMethodInvocation.class)) {
      _nodes.add(node);
    }
    return true;
  }

  public boolean visit(SuperConstructorInvocation node) {
    if (_target.equals(SuperConstructorInvocation.class)) {
      _nodes.add(node);
    }
    return true;
  }

  public boolean visit(ArrayCreation node) {
    if (_target.equals(ArrayCreation.class)) {
      _nodes.add(node);
    }
    return true;
  }

  public boolean visit(FieldAccess fieldAccess) {
    if (_target.equals(FieldAccess.class)) {
      _nodes.add(fieldAccess);
    }
    return true;
  }

  public boolean visit(ReturnStatement node) {
    if (_target.equals(ReturnStatement.class)) {
      _nodes.add(node);
    }
    return true;
  }

}
