package cofix.core.modification;

import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.WrongPatchException;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;

public class InfixExprModifyEvent extends ModificationEvent {

  private AbstractToken _token;
  private enum Type{
    LeftOperand,
    RightOperand,
    Operator
  }
  private Type _type;


  public InfixExprModifyEvent(ASTNode node, AbstractToken target) {
    if(node instanceof InfixExpression) {
      _token = target;
      _type = Type.Operator;
    }
    else if(node.getLocationInParent().equals("leftOperand")) {
      _token = target;
      _type = Type.LeftOperand;
    }else if(node.getLocationInParent().equals("rightOperand")) {
      _token = target;
      _type = Type.RightOperand;
    }
  }

  @Override
  public boolean tryApply(ASTNode originalRoot, BuggyFile buggyFile, Set<String> availableVars,
      List<Modification> modifications) throws Exception {
    if(originalRoot == null){return false;}
    StructureVisitor visitor = new StructureVisitor(InfixExpression.class);
    originalRoot.accept(visitor);

    for (ASTNode node : visitor.getNodes()) {
      if(_type == Type.Operator){
        try{
          Modification modi = new Modification(buggyFile);
          modi.replace(node,
              _token.buildNode(node,buggyFile,availableVars).getFirst());
          modifications.add(modi);
        }catch(WrongPatchException e){
          //Inavaliable
        }
      }
    }
    return true;
  }
}
