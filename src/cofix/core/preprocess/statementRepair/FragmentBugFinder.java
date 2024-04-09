package cofix.core.preprocess.statementRepair;

//public class FragmentBugFinder extends ASTVisitor {
//  private final CompilationUnit _cu;
//  private final int _line;
//  private ASTNode _bugStatement;
//
//
//  public FragmentBugFinder(int _line, CompilationUnit _cu, FragmentProcessor processor) {
//    this._line = _line;
//    this._cu = _cu;
//  }
////todo:specific
//  public boolean visit(Block method) {
//    int start = _cu.getLineNumber(method.getStartPosition());
//    int end = _cu.getLineNumber(method.getStartPosition() + method.getLength());
//    int gap=method.getLength();
//    if (start <= _line && _line <= end) {
//      if (_bugStatement == null) {
//          _bugStatement = method;
//      }
//    }
//    return true;
//
//  }
//  public ASTNode getBugStatment() {
//    return _bugStatement;
//  }
//
//}

import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.stmt.SwCase;
import cofix.core.parser.search.CodeSearch;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

public class FragmentBugFinder extends ASTVisitor {

  private final CompilationUnit _cu;
  private final int _line;
  private ASTNode _bugStatement;
  private CodeBlock _codeBlock;


  public FragmentBugFinder(int _line, CompilationUnit _cu) {
    this._line = _line;
    this._cu = _cu;
    _cu.accept(this);
  }


  public boolean visit(Block method) {
    int start = _cu.getLineNumber(method.getStartPosition());
    int end = _cu.getLineNumber(method.getStartPosition() + method.getLength());
    int gap = method.getLength();
    if (start <= _line && _line <= end) {
      if (_bugStatement == null) {
        _bugStatement = method;
      }
    }
    return true;

  }

  public boolean visit(IfStatement method) {
    int start = _cu.getLineNumber(method.getStartPosition());
    int end = _cu.getLineNumber(method.getStartPosition() + method.getLength());
    int gap = method.getLength();
    if (start <= _line && _line <= end) {
      if (_bugStatement == null) {
        _bugStatement = method;
      }
    }
    return true;

  }


  public ASTNode getBugStatment() {
    return _bugStatement;
  }

  public ASTNode getBuggyNode(String fileName) {
    CodeSearch codeSearch = new CodeSearch(_cu, _line, 5, null, 3);
    CodeBlock codeBlock = new CodeBlock(fileName, _cu, codeSearch.getASTNodes());
    if (codeBlock.getParsedNode().size() == 1) {
      if (codeBlock.getParsedNode().get(0) instanceof SwCase) {
        return codeBlock.getParsedNode().get(0).getOriginalAST().getParent();
      } else {
        return codeBlock.getParsedNode().get(0).getOriginalAST();
      }
    } else if (codeBlock.getParsedNode().size() > 1) {
      return codeBlock.getParsedNode().get(0).getOriginalAST().getParent();
    } else {
      return null;
    }
  }

  public Block getBuggyBlock(String fileName) {
    //deprecate
    // CodeSearch codeSearch=new CodeSearch(_cu,_line,5,null,3);
    CodeSearch codeSearch = new CodeSearch(_cu, _line, 5, null, 3);
    CodeBlock codeBlock = new CodeBlock(fileName, _cu, codeSearch.getASTNodes());
    if (codeBlock.getParsedNode().size() >= 1) {
      Block newBlock = codeBlock.getParsedNode().get(0).getOriginalAST().getAST().newBlock();
      List<ASTNode> nodeList = new ArrayList<ASTNode>();
      for (Node node : codeBlock.getParsedNode()) {
        nodeList.add(node.getOriginalAST());
        if (node instanceof SwCase) {
          for (Node child : node.getChildren()) {
            nodeList.add(child.getOriginalAST());
          }
        }
      }
      for (ASTNode x : nodeList) {
        Statement statementCopy = (Statement) ASTNode.copySubtree(newBlock.getAST(),
            x);
        newBlock.statements().add(statementCopy);
      }
      return newBlock;
    }
    return null;
  }

  public ASTNode getSimilarNode(String fileName, int _line) {
    //deprecate
    CodeSearch codeSearch = new CodeSearch(_cu, _line, 5, null, 3);
    CodeBlock codeBlock = new CodeBlock(fileName, _cu, codeSearch.getASTNodes());
    if (codeBlock.getParsedNode().size() == 1) {
      return codeBlock.getParsedNode().get(0).getOriginalAST();
    } else if (codeBlock.getParsedNode().size() > 1) {
      return codeBlock.getParsedNode().get(0).getOriginalAST().getParent();
    } else {
      return null;
    }
  }

  public Block getSimilarBlock(String fileName, int _line) {
    //deprecate
    // CodeSearch codeSearch=new CodeSearch(_cu,_line,5,null,3);
    CodeSearch codeSearch = new CodeSearch(_cu, _line, 5, null, 3);
    CodeBlock codeBlock = new CodeBlock(fileName, _cu, codeSearch.getASTNodes());
    if (codeBlock.getParsedNode().size() >= 1) {

      Block newBlock = codeBlock.getParsedNode().get(0).getOriginalAST().getAST().newBlock();
      List<ASTNode> nodeList = new ArrayList<ASTNode>();
      for (Node node : codeBlock.getParsedNode()) {
        nodeList.add(node.getOriginalAST());
        if (node instanceof SwCase) {
          for (Node child : node.getChildren()) {
            nodeList.add(child.getOriginalAST());
          }
        }
      }
      for (ASTNode x : nodeList) {
        Statement statementCopy = (Statement) ASTNode.copySubtree(newBlock.getAST(),
            x);
        newBlock.statements().add((Statement) ASTNode.copySubtree(x.getAST(),
            x));
        // newBlock.statements().add(statementCopy);
      }
      return newBlock;
    }
    return null;
  }

  public Set<String> getAvailableVars(String fileName) {
    Set<String> result = new HashSet<>();
    for (Entry<String, Type> vars : NodeUtils.getUsableVarTypes(fileName, _line).entrySet()) {
      result.add(vars.getKey());
    }
    return result;
  }


}
