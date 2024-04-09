package org.example;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.PatternExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.stmt.YieldStmt;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import java.util.List;

public class StmtVisitorBackup extends GenericListVisitorAdapter<Node, Void> {


  @Override
  public List<Node> visit(ArrayAccessExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ArrayCreationExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ArrayInitializerExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(AssignExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(BinaryExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(BooleanLiteralExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(CastExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ClassExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ConditionalExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ContinueStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(DoubleLiteralExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(EmptyStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(EnclosedExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ExpressionStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(FieldAccessExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(FieldDeclaration n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(InstanceOfExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(IntegerLiteralExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(LabeledStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(LambdaExpr n, Void arg) {
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
  public List<Node> visit(LongLiteralExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(MarkerAnnotationExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(MethodCallExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(MethodReferenceExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(NameExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(NullLiteralExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ReturnStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(StringLiteralExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(SuperExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ThisExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(ThrowStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(TypeExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(UnaryExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(VariableDeclarationExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(UnparsableStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(SwitchExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(YieldStmt n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(TextBlockLiteralExpr n, Void arg) {
    return List.of(n);
  }

  @Override
  public List<Node> visit(PatternExpr n, Void arg) {
    return List.of(n);
  }

}

