package cofix.core.modification;

import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.WrongPatchException;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ParamReplaceEvent extends ModificationEvent {

  private int _index;

  private AbstractToken _token;

  public ParamReplaceEvent(int index, AbstractToken replaceToken) {
    _index = index;
    _token = replaceToken;
  }

  @Override
  public boolean tryApply(ASTNode originalRoot, BuggyFile buggyFile,
      Set<String> availableVars, List<Modification> modifications) throws Exception {
    if(originalRoot == null){
      return false;
    }

    StructureVisitor visitor = new StructureVisitor(MethodInvocation.class);
    originalRoot.accept(visitor);

    for (ASTNode node : visitor.getNodes()) {
      try{
        ASTNode root = ASTNode.copySubtree(_ast, node);

        List paras = ModificationEvent.getArgumentsList(root);

        if(_index >= paras.size()){
          continue;
        }

        Modification modi = new Modification(buggyFile);

        ASTNode targetNode = ASTNode.copySubtree(_ast, _token.buildNode(fakeSimpleName,buggyFile,availableVars).getFirst());

        paras.set(_index, targetNode);

        modi.replace(node, root);
        modifications.add(modi);

      }catch (WrongPatchException e) {
        System.out.println("WrongPatchException");
      }
    }
    return true;
  }
}
