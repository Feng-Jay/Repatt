package cofix.core.modification;

import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.WrongPatchException;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodReplaceEvent extends ModificationEvent {

  private AbstractToken _targetMethod;

  public MethodReplaceEvent(AbstractToken targetMethod) {
    _targetMethod = targetMethod;
  }

  @Override
  public boolean tryApply(ASTNode originalRoot, BuggyFile buggyFile, Set<String> availableVars,
      List<Modification> modifications) throws Exception {
    if(originalRoot == null){return false;}
    StructureVisitor visitor = new StructureVisitor(MethodInvocation.class);
    originalRoot.accept(visitor);
    for (ASTNode node : visitor.getNodes()) {
      try{
        Modification modi = new Modification(buggyFile);
        modi.replace(node, _targetMethod.buildNode(node,buggyFile,availableVars).getFirst());
        modifications.add(modi);
      }catch(WrongPatchException e){
        //Inavaliable
      }
    }
    return true;
  }
}
