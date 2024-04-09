package org.example;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.YieldStmt;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import java.util.List;

public class StmtVisitor extends GenericListVisitorAdapter<Node, Void> {


  @Override
  public List<Node> visit(ContinueStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(DoStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(EmptyStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ExplicitConstructorInvocationStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ExpressionStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ForStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ForEachStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(IfStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(LabeledStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(LocalClassDeclarationStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(LocalRecordDeclarationStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ReturnStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(SwitchStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ThrowStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(TryStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(WhileStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(UnparsableStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(YieldStmt n, Void arg) {
    return List.of(n);
  }

}

