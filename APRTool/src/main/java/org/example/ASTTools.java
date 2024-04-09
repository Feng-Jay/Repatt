package org.example;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericListVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import me.tongfei.progressbar.ProgressBar;

public class ASTTools {

  public static int countLeafNodes(Node node){
    if (node.getChildNodes().isEmpty()){
      return 1;
    }
    int count = 0;
    for (Node child : node.getChildNodes()){
      count += countLeafNodes(child);
    }
    return count;
  }

  public static Set<Node> getUpdatedStmts(CompilationUnit src, CompilationUnit dst){
    StmtVisitor stmtVisitor = new StmtVisitor();
    List<Node> srcStmts = stmtVisitor.visit(src, null);
    List<Node> dstStmts = stmtVisitor.visit(dst, null);
    Iterator<Node> srcStmtsIter = srcStmts.iterator();
    while(srcStmtsIter.hasNext()){
      Node srcStmt = srcStmtsIter.next();
      Iterator<Node> dstStmtsIter = dstStmts.iterator();
      while(dstStmtsIter.hasNext()){
        Node dstStmt = dstStmtsIter.next();
        if(srcStmt.toString().equals(dstStmt.toString())){
          srcStmtsIter.remove();
          dstStmtsIter.remove();
          break;
        }
      }
    }
    return new HashSet<>(dstStmts);
  }

  public static Pair<Set<Node>, Set<Node>> breakDown(Set<Node> nodes, Set<SimpleName> newlyDeclaredVariableNames){
    Set<Node> after = new HashSet<>();
    Set<Node> left = new HashSet<>();
    for (Node node : nodes) {
      if (node.getChildNodes().isEmpty()){
        if(!newlyDeclaredVariableNames.contains(node)){
          System.out.println("Can't find " + node.toString());
          left.add(node);
        }
        continue;
      }
      after.addAll(node.getChildNodes());
    }
    return new Pair<>(after, left);
  }

  public static Map<Node, Integer> searchInCU(List<CompilationUnit> cus, Set<Node> targets){
    List<Node> found = new ArrayList<>();
    Set<SimpleName> newlyDeclaredVariables = extractNewlyDeclaredVariable(targets);
    while(!targets.isEmpty()){
      GreedyFindVisitor visitor = new GreedyFindVisitor();
      for (CompilationUnit cu : cus){
        found.addAll(visitor.visit(cu, targets));
      }
      Pair<Set<Node>, Set<Node>> afterBreak = breakDown(targets, newlyDeclaredVariables);
      targets = afterBreak.first;
      if (!afterBreak.second.isEmpty()){
        return null;
      }
    }
    Map<Node, Integer> foundMap = new HashMap<>();
    for (Node node : found){
      foundMap.put(node, countLeafNodes(node));
    }
    return foundMap;
  }

  public static Set<SimpleName> extractNewlyDeclaredVariable(Set<Node> nodes) {
    GenericListVisitorAdapter<SimpleName, Void> varVisitor = new GenericListVisitorAdapter<>() {
      @Override
      public List<SimpleName> visit(VariableDeclarationExpr n, Void arg) {
        List<SimpleName> newlyName = new ArrayList<>();
        for (VariableDeclarator variable : n.getVariables()) {
          newlyName.add(variable.getName());
        }
        return newlyName;
      }
    };
    Set<SimpleName> exprs = new HashSet<>();
    for (Node n: nodes){
      exprs.addAll(
          n.accept(varVisitor, null)
      );
    }
    return exprs;
  }

}
