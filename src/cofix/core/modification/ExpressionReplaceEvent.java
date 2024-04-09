package cofix.core.modification;

import cofix.core.preprocess.token.AbstractToken;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ReturnStatement;

public class ExpressionReplaceEvent extends ModificationEvent {

  private AbstractToken _targetMethod;
  private Class _originClass;

  public ExpressionReplaceEvent(Class originType, AbstractToken targetMethod) {
    _targetMethod = targetMethod;
    _originClass = originType;
  }

  @Override
  public boolean tryApply(ASTNode originalRoot, BuggyFile buggyFile, Set<String> availableVars,
      List<Modification> modifications) throws Exception {
    if (originalRoot == null) {
      return false;
    }
    StructureVisitor visitor = new StructureVisitor(_originClass);
    originalRoot.accept(visitor);
    for (ASTNode node : visitor.getNodes()) {
      Modification modi = new Modification(buggyFile);
      if (node instanceof ReturnStatement) {
        modi.replace(((ReturnStatement) node).getExpression(),
            _targetMethod.buildNode(node, buggyFile, availableVars).getFirst());
      } else {
        return false;
      }
      modifications.add(modi);
    }
    return true;
  }
}
