package cofix.core.modification;

import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.WrongPatchException;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.SimpleName;

public class FieldAccessModiEvent extends ModificationEvent {
  AbstractToken _targetField;

  public FieldAccessModiEvent(AbstractToken targetField) {
    _targetField = targetField;
  }

  public boolean tryApply(ASTNode originalRoot, BuggyFile buggyFile, Set<String> availableVars,
      List<Modification> modifications) throws Exception {
    if(originalRoot == null){return false;}
    StructureVisitor visitor = new StructureVisitor(FieldAccess.class);
    originalRoot.accept(visitor);
    for (ASTNode node : visitor.getNodes()) {
      try{
        Modification modi = new Modification(buggyFile);
        modi.replace(node, ASTNode.copySubtree(node.getAST(), _targetField.buildNode(node,buggyFile,availableVars).getFirst()));
        modifications.add(modi);
      }catch(WrongPatchException e){
        //Inavaliable
      }
    }
    return true;
  }

}
