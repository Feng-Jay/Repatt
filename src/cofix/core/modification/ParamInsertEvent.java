package cofix.core.modification;

import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.WrongPatchException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ExpressionStatement;

public class ParamInsertEvent extends ModificationEvent {

  private final List<AbstractToken> _targetTokens;

  private final int _index;

  public ParamInsertEvent(List<AbstractToken> targetToken, int index) {
    _targetTokens = targetToken;
    _index = index;
  }

  public boolean tryApply(ASTNode originalRoot, BuggyFile buggyFile, Set<String> availableVars, List<Modification> modifications)
      throws Exception {

    if(originalRoot == null){return false;}

    if(originalRoot instanceof ExpressionStatement) {
      originalRoot = ((ExpressionStatement)originalRoot).getExpression();
    }

    ASTNode root = ASTNode.copySubtree(_ast, originalRoot);

    List paras = ModificationEvent.getArgumentsList(root);

    if (paras == null) {
      return false;
    }

    if(_index >= paras.size()){
      return false;
    }

    for (AbstractToken token : _targetTokens) {
      try{
        Modification modi = new Modification(buggyFile);
        ASTNode node2Insert = ASTNode.copySubtree(_ast,token.buildNode(fakeSimpleName,buggyFile,availableVars).getFirst());
        paras.add(_index, node2Insert);
        modi.replace(originalRoot,root);
        modifications.add(modi);
      }catch(WrongPatchException e){
        System.out.println("WrongPatchException");
      }
    }
    return true;
  }



}
