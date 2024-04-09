package cofix.core.modification;

import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;

public class ParamDeleteEvent extends ModificationEvent {

  @Override
  public boolean tryApply(ASTNode originalRoot, BuggyFile buggyFile, Set<String> availableVars,
      List<Modification> modifications) throws Exception {
    return false;
  }
}
