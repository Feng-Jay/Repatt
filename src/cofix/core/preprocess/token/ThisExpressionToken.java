package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ThisExpression;

public class ThisExpressionToken extends AbstractToken{

  private final Name _qualifier;

  public ThisExpressionToken(ThisExpression thisExpression){
    super(thisExpression);
    _name = "this";
    ITypeBinding typeBinding = thisExpression.resolveTypeBinding();
    _type = typeBinding==null ? "Failed" : typeBinding.getName();
    _qualifier = thisExpression.getQualifier();
  }

  public Name getQualifier(){
    return _qualifier;
  }

  @Override
  public boolean equals(Object obj) {
    if(_qualifier == null){
      return obj instanceof ThisExpressionToken && ((ThisExpressionToken)obj).getType().equals(_type) && ((ThisExpressionToken)obj).getQualifier() == null;
    }
    return obj instanceof ThisExpressionToken && ((ThisExpressionToken)obj).getType().equals(_type) && ((ThisExpressionToken)obj).getQualifier()!= null &&((ThisExpressionToken)obj).getQualifier().getFullyQualifiedName().equals(_qualifier.getFullyQualifiedName());
  }

  @Override
  public int hashCode(){
    if (_qualifier == null){
      return super.hashCode();
    }
    return super.hashCode() + _qualifier.getFullyQualifiedName().hashCode();
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws WrongPatchException {
    if(_qualifier != null && !availableVars.contains(_qualifier.getFullyQualifiedName())){
      throw new WrongPatchException();
    }
    ThisExpression target = _ast.newThisExpression();
    if(_qualifier!=null){
      target.setQualifier((Name) ASTNode.copySubtree(_ast,_qualifier));
    }
    return new Pair(target,1.0);
  }

  @Override
  public boolean isLeafNode() {
    return true;
  }
}
