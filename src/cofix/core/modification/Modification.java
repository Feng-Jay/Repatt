package cofix.core.modification;


import static cofix.core.preprocess.statementRepair.FragmentProcessor.InsertType.insertCondition;

import cofix.core.preprocess.statementRepair.FragmentProcessor.InsertType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEdit;

public class Modification {

  private final BuggyFile _buggyFile;
  private ASTRewrite _rewriter;

  public Modification(BuggyFile buggyFile) {
    _buggyFile = buggyFile;
    _rewriter = ASTRewrite.create(buggyFile.getCompilationUnit().getAST());
  }

  public ASTNode replace(ASTNode original, ASTNode fixed) {
    ASTNode node = ASTNode.copySubtree(original.getAST(), fixed);
    _rewriter.replace(original, node, null);
    return node;
  }

//  public void replaceStmt(ASTNode original, ASTNode fixed) {
//    ASTNode node = ASTNode.copySubtree(original.getAST(), fixed);
//    //只能用条件换条件
//    if (original.getParent() instanceof IfStatement) {
//      if (fixed.getParent() instanceof IfStatement) {
//        _rewriter.replace(original, node, null);
//      }
//    } else {
//      _rewriter.replace(original, node, null);
//    }
//
//  }

  @SuppressWarnings("unchecked")
//  public void insertAfter(Statement previousElement, Statement target) {
//    ASTNode node = ASTNode.copySubtree(previousElement.getAST(), target);
//    if (previousElement.getParent() instanceof Block) {
//      Block block = (Block) ASTNode.copySubtree(_rewriter.getAST(), previousElement.getParent());
//      ListRewrite lrw = _rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
//      lrw.insertAfter(node, previousElement, null);
//    } else {
//      Statement statementCopy = (Statement) ASTNode.copySubtree(previousElement.getAST(),
//          previousElement);
//      Block newBlock = previousElement.getAST().newBlock();
//      newBlock.statements().add(statementCopy);
//      newBlock.statements().add(node);
//      _rewriter.replace(previousElement, newBlock, null);
//    }
//  }

//  public void insert(ASTNode previous, ASTNode oriTarget, InsertType type) {
//    //previous变成完整的语句，previousBlock里面添加
//    if(previous !=null) {
//      while (!(previous instanceof Statement)) {
//        previous = previous.getParent();
//      }
//      ASTNode previousBlock = previous.getParent();
//      while (!(previousBlock instanceof Block)) {
//        previousBlock = previousBlock.getParent();
//      }
//
//      AST ast = previous.getAST();
//      Block block = (Block) ASTNode.copySubtree(ast, previousBlock);
//      Block newBlock = ast.newBlock();
//      Statement statementCopy = (Statement) ASTNode.copySubtree(ast, previous);
//      //ListRewrite lrw = _rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
//
//      if (type == InsertType.insertCondition) {
//        //previous , insert(if) ,thenStatement
//        //replace
//        IfStatement ifStmt = ast.newIfStatement();
//        //ast
//        ifStmt.setExpression((Expression) ASTNode.copySubtree(ast, oriTarget));
//        //todo...
//        //ifStmt.setThenStatement();
//        newBlock.statements().add(ifStmt);
//        _rewriter.replace(previous, newBlock, null);
//      } else {
//        Statement insertStmt = null;
//        if (oriTarget instanceof Expression) {
//          insertStmt = ast.newExpressionStatement((Expression) ASTNode.copySubtree(ast, oriTarget));
//        } else {
//          insertStmt = (Statement) ASTNode.copySubtree(ast, oriTarget);
//        }
//        switch (type) {
//          case insertHead:
//            //for insertHand,previous is rear
//            // lrw.insertBefore(insertStmt, previous,null);
//            newBlock.statements().add(insertStmt);
//            newBlock.statements().add(statementCopy);
//            _rewriter.replace(previous, newBlock, null);
//            break;
//          case insert:
//            //lrw.insertAfter(insertStmt, previous, null);
//            newBlock.statements().add(statementCopy);
//            newBlock.statements().add(insertStmt);
//            _rewriter.replace(previous, newBlock, null);
//            break;
//        }
//      }
//    }
//  }

  public Block insertBody(ASTNode condition, ASTNode body, ASTNode rear) {
    AST ast = rear.getAST();
    Block newBlock = ast.newBlock();
    IfStatement ifStmt = ast.newIfStatement();
    ifStmt.setExpression((Expression) ASTNode.copySubtree(ast, condition));
    ifStmt.setThenStatement((Statement) ASTNode.copySubtree(ast, body));
    newBlock.statements().add(ifStmt);
    newBlock.statements().add((Statement) ASTNode.copySubtree(ast, rear));
    _rewriter.replace(rear, newBlock, null);
    return newBlock;
  }

  public Block insertBefore(ASTNode oriNode, ASTNode target, InsertType type) {
    //oriNode stmt
    //target
    AST ast = oriNode.getAST();
    Block newBlock = ast.newBlock();
    Statement statementCopy = (Statement) ASTNode.copySubtree(ast, oriNode);
    Statement insertStmt = null;
    if (type == insertCondition) {
      //insert condition
      IfStatement ifStmt = ast.newIfStatement();
      ifStmt.setExpression((Expression) ASTNode.copySubtree(ast, target));
      ifStmt.setThenStatement((Statement) ASTNode.copySubtree(ast, oriNode));
      newBlock.statements().add(ifStmt);
      _rewriter.replace(oriNode, newBlock, null);
      return null;
    } else {
      // insert
      if (target instanceof VariableDeclarationFragment) {
        target = target.getParent();
      }
      if (!(target instanceof Statement)) {
        insertStmt = ast.newExpressionStatement((Expression) ASTNode.copySubtree(ast, target));
      } else {
        insertStmt = (Statement) ASTNode.copySubtree(ast, target);
      }
      newBlock.statements().add(insertStmt);
      newBlock.statements().add(statementCopy);
      _rewriter.replace(oriNode, newBlock, null);
    }
    return newBlock;
  }

  public Block insertAfter(ASTNode oriNode, ASTNode target) {
    AST ast = oriNode.getAST();
    Block newBlock = ast.newBlock();
    Statement statementCopy = (Statement) ASTNode.copySubtree(ast, oriNode);
    Statement insertStmt = null;
    if (target instanceof Expression) {
      insertStmt = ast.newExpressionStatement((Expression) ASTNode.copySubtree(ast, target));
    } else {
      insertStmt = (Statement) ASTNode.copySubtree(ast, target);
    }
    newBlock.statements().add(statementCopy);
    newBlock.statements().add(insertStmt);
    _rewriter.replace(oriNode, newBlock, null);
    return newBlock;
  }


//  public void insert(ASTNode rear, ASTNode oriTarget, InsertType type) {
//    AST ast = rear.getAST();
//
//    Block newBlock = ast.newBlock();
//    while (!(rear instanceof Statement)) {//todo:需要调试previous is condition 加入body第一句或 默认值
//      rear = rear.getParent();
//    }
//    Statement statementCopy = (Statement) ASTNode.copySubtree(ast, rear);
//    Statement insertStmt = null;
//
//    //get insertStmt
//    if (type == insert || type == insertHead) {
//      if (oriTarget instanceof Expression) {
//        insertStmt = ast.newExpressionStatement((Expression) ASTNode.copySubtree(ast, oriTarget));
//      } else {
//        insertStmt = (Statement) ASTNode.copySubtree(ast, oriTarget);
//      }
//    }
//
//    switch (type) {
//      case insertCondition:
//        //get ifstmt
//        IfStatement ifStmt = ast.newIfStatement();
//        ifStmt.setExpression((Expression) ASTNode.copySubtree(ast, oriTarget));
//        ifStmt.setThenStatement((Statement) ASTNode.copySubtree(ast, rear));
//        newBlock.statements().add(ifStmt);
//        _rewriter.replace(rear, newBlock, null);
//        break;
//      //insert
//      case insertHead:
//        newBlock.statements().add(statementCopy);
//        newBlock.statements().add(insertStmt);
//        _rewriter.replace(rear, newBlock, null);
//        break;
//      case insert:
//        newBlock.statements().add(insertStmt);
//        newBlock.statements().add(statementCopy);
//        //  newBlock.statements().add(insertStmt);
////        newBlock.statements().add(insertStmt);
//        _rewriter.replace(rear, newBlock, null);
//        break;
//    }
//
//  }


  @SuppressWarnings("unchecked")
//  public void insertBefore(Statement previousElement, Statement target) {
//    ASTNode node = ASTNode.copySubtree(previousElement.getAST(), target);
//    if (previousElement.getParent() instanceof Block) {
//      Block block = (Block) previousElement.getParent();
//      ListRewrite lrw = _rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
//      lrw.insertBefore(node, previousElement, null);
//    } else {
//      Statement statementCopy = (Statement) ASTNode.copySubtree(previousElement.getAST(),
//          previousElement);
//      Block newBlock = previousElement.getAST().newBlock();
//      newBlock.statements().add(node);
//      newBlock.statements().add(statementCopy);
//      _rewriter.replace(previousElement, newBlock, null);
//    }
//  }


  public void delete(Statement statement) {
    if (statement.getParent() instanceof Block) {
      Block block = (Block) statement.getParent();
      ListRewrite lrw = _rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
      lrw.remove(statement, null);
    } else {
      Statement emptyStatement = statement.getAST().newEmptyStatement();
      _rewriter.replace(statement, emptyStatement, null);
    }
  }

  public TextEdit buildPatch() {
    TextEdit temp = _rewriter.rewriteAST(_buggyFile.getDocument(), null);
    _rewriter = ASTRewrite.create(_buggyFile.getCompilationUnit().getAST());
    return temp;
  }
}
