package org.example;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
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
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.PatternExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsDirective;
import com.github.javaparser.ast.modules.ModuleOpensDirective;
import com.github.javaparser.ast.modules.ModuleProvidesDirective;
import com.github.javaparser.ast.modules.ModuleRequiresDirective;
import com.github.javaparser.ast.modules.ModuleUsesDirective;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
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
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.UnparsableStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.YieldStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GreedyFindVisitor extends GenericListVisitorAdapter<Node, Set<Node>> {

  private static List<Node> addAllWithReturn(List<Node> a, List<Node> b){
    a.addAll(b);
    return a;
  }

  private List<Node> checkIfEquals(Node n, Set<Node> arg) {
    List<Node> found = new ArrayList<>();
    for (Node node : arg) {
      if (node.equals(n)) {
        found.add(node);
        arg.remove(node);
        break;
      }
    }
    return found;
  }

  @Override
  public List<Node> visit(AnnotationDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(AnnotationMemberDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ArrayAccessExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ArrayCreationExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ArrayCreationLevel n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ArrayInitializerExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ArrayType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(AssertStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(AssignExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(BinaryExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(BlockComment n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(BlockStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(BooleanLiteralExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(BreakStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(CastExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(CatchClause n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(CharLiteralExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ClassExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ClassOrInterfaceDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ClassOrInterfaceType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(CompilationUnit n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ConditionalExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ConstructorDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ContinueStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(DoStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(DoubleLiteralExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(EmptyStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(EnclosedExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(EnumConstantDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(EnumDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ExplicitConstructorInvocationStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  //ExpressionStmt
  @Override
  public List<Node> visit(ExpressionStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(FieldAccessExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(FieldDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ForStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ForEachStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(IfStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ImportDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(InitializerDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(InstanceOfExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(IntegerLiteralExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(IntersectionType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(JavadocComment n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(LabeledStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(LambdaExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(LineComment n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(LocalClassDeclarationStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(LocalRecordDeclarationStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(LongLiteralExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(MarkerAnnotationExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(MemberValuePair n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(MethodCallExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(MethodDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(MethodReferenceExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(NameExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(Name n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(NormalAnnotationExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(NullLiteralExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ObjectCreationExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(PackageDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(Parameter n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(PrimitiveType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ReturnStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(SimpleName n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(SingleMemberAnnotationExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(StringLiteralExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(SuperExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(SwitchEntry n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(SwitchStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(SynchronizedStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ThisExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ThrowStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(TryStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(TypeExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(TypeParameter n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(UnaryExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(UnionType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(UnknownType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(VariableDeclarationExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(VariableDeclarator n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(VoidType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(WhileStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(WildcardType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ModuleDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ModuleExportsDirective n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ModuleOpensDirective n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ModuleProvidesDirective n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ModuleRequiresDirective n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ModuleUsesDirective n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(UnparsableStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(ReceiverParameter n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(VarType n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(Modifier n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(SwitchExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(YieldStmt n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(TextBlockLiteralExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(PatternExpr n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(RecordDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }

  @Override
  public List<Node> visit(CompactConstructorDeclaration n, Set<Node> arg) {
    return addAllWithReturn(checkIfEquals(n, arg), super.visit(n, arg));
  }


}
