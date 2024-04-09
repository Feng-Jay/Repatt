package cofix.core.preprocess.statementRepair;

import cofix.common.util.DuoMap;
import cofix.core.preprocess.Identifier;
import cofix.core.preprocess.pattern.Fragment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class StatementProcessor {

  private DuoMap<Integer, Fragment> _statementsId = new DuoMap<>();
  private DuoMap<Integer, String> _statementsNameId = new DuoMap<>();
  private final Identifier _stmtIdentifier = new Identifier(_statementsId, _statementsNameId);

  public Identifier getIdentifier() {
    return _stmtIdentifier;
  }

  public void collectCu(ASTNode node) {
    FragmentVisitor fragmentVisitor = new FragmentVisitor();
    node.accept(fragmentVisitor);
  }

  public DuoMap<Integer, Fragment> getIdMap() {
    return _statementsId;
  }

  public DuoMap<Integer, String> getNameMap() {
    return _statementsNameId;
  }

  public class FragmentVisitor extends ASTVisitor {

    //public IdOpType _type;

//    FragmentVisitor(IdOpType type) {
//      _type = type;
//    }

    public boolean visit(IfStatement node) {

      process(node);
      return false;
    }

    public boolean visit(ReturnStatement node) {
      process(node);

      return false;
    }

    public boolean visit(ForStatement node) {
      process(node);
      return false;
    }

    public boolean visit(ExpressionStatement node) {
      process(node);
      return false;
    }

    public boolean visit(EnhancedForStatement node) {
      process(node);
      return false;
    }

    public boolean visit(DoStatement node) {
      process(node);
      return false;
    }

    public boolean visit(WhileStatement node) {
      process(node);
      return false;
    }

    public boolean visit(VariableDeclarationStatement node) {
      process(node);
      return false;
    }

    public void process(ASTNode node) {
     // if (_type==IdOpType.setId) {
        _stmtIdentifier.setId(node, node.toString());
//      }else if(_type==IdOpType.addId){
//        _stmtIdentifier.addNodeToBugSeq(node);
    //  }
    }

  }
}

