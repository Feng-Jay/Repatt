package cofix.core.modification;

import cofix.core.preprocess.token.AbstractToken;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;

public class ArrayCreationModifyEvent extends ModificationEvent {

  public enum modifyType{ModifyArrayType,ModifyDimension};
  private AbstractToken _targetToken;
  private modifyType _modifyType;

  public ArrayCreationModifyEvent(AbstractToken targetToken, modifyType modifyType) {
    _targetToken = targetToken;
    _modifyType = modifyType;
  }


  @Override
  public boolean tryApply(ASTNode originalRoot, BuggyFile buggyFile, Set<String> availableVars,
      List<Modification> modifications) throws Exception {
    if(originalRoot == null){return false;}
    StructureVisitor visitor = new StructureVisitor(ArrayCreation.class);
    originalRoot.accept(visitor);
    for (ASTNode node : visitor.getNodes()) {
      if(_modifyType == modifyType.ModifyDimension){
        Modification modi = new Modification(buggyFile);
        modi.replace(((ASTNode) ((ArrayCreation)node).dimensions().get(0)),
            _targetToken.buildNode(node, buggyFile, availableVars).getFirst());
        modifications.add(modi);
      }
    }
    return false;
  }
}
