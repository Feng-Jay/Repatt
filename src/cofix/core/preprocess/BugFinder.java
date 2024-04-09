package cofix.core.preprocess;

import java.util.HashMap;
import java.util.Map;
import javax.swing.plaf.nimbus.State;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;

public class BugFinder extends ASTVisitor {

  private final CompilationUnit _cu;
  private final int _line;
  private ASTNode _bugStatement;
  private final Map<Integer, ASTNode> _nodeMap = new HashMap<>();

  public BugFinder(int _line, CompilationUnit _cu) {
    this._line = _line;
    this._cu = _cu;
    _cu.accept(this);
  }

  public static ASTNode extractFixablePart(ASTNode node) {
    ASTNode bugStmt = null;
    while (!(node instanceof Statement)) {
      if (node.getParent() == null) {
        return null;
      }
      node = node.getParent();
    }
    if (node instanceof IfStatement) {
      bugStmt = ((IfStatement) node).getExpression();
    } else if (node instanceof WhileStatement) {
      bugStmt = ((WhileStatement) node).getExpression();
    } else if (node instanceof ForStatement) {
      bugStmt = ((ForStatement) node).getExpression();
    } else if (node instanceof EnhancedForStatement) {
      bugStmt = ((EnhancedForStatement) node).getExpression();
    } else if (node instanceof SwitchCase) {
      bugStmt = ((SwitchCase) node).getExpression();
    } else {
      bugStmt = node;
    }
    return bugStmt;
  }


  public boolean visit(SimpleName simpleName) {
    int start = _cu.getLineNumber(simpleName.getStartPosition());
    int end = _cu.getLineNumber(simpleName.getStartPosition() + simpleName.getLength());
    if (start <= _line && _line <= end) {
      if (_bugStatement == null) {
        ASTNode node = simpleName;
        _bugStatement = extractFixablePart(node);
      }
    }
    return true;
  }

  public boolean visit(QualifiedName qualifiedName) {
    int start = _cu.getLineNumber(qualifiedName.getStartPosition());
    int end = _cu.getLineNumber(qualifiedName.getStartPosition() + qualifiedName.getLength());
    if (start <= _line && _line <= end) {
      if (_bugStatement == null) {
        ASTNode node = qualifiedName;
        _bugStatement = extractFixablePart(node);
      }
    }
    return true;
  }

  public boolean visit(BooleanLiteral booleanLiteral) {
    int start = _cu.getLineNumber(booleanLiteral.getStartPosition());
    int end = _cu.getLineNumber(booleanLiteral.getStartPosition() + booleanLiteral.getLength());
    if (start <= _line && _line <= end) {
      if (_bugStatement == null) {
        ASTNode node = booleanLiteral;
        _bugStatement = extractFixablePart(node);
      }
    }
    return false;
  }

  public boolean visit(NumberLiteral numberLiteral) {
    int start = _cu.getLineNumber(numberLiteral.getStartPosition());
    int end = _cu.getLineNumber(numberLiteral.getStartPosition() + numberLiteral.getLength());
    if (start <= _line && _line <= end) {
      if (_bugStatement == null) {
        ASTNode node = numberLiteral;
        _bugStatement = (node);
      }
    }
    return false;
  }

  public boolean visit(CharacterLiteral characterLiteral) {
    int start = _cu.getLineNumber(characterLiteral.getStartPosition());
    int end = _cu.getLineNumber(characterLiteral.getStartPosition() + characterLiteral.getLength());
    if (start <= _line && _line <= end) {
      if (_bugStatement == null) {
        ASTNode node = characterLiteral;
        _bugStatement = extractFixablePart(node);
      }
    }
    return false;
  }


  public ASTNode getBugStatment() {
    return _bugStatement;
  }
}
