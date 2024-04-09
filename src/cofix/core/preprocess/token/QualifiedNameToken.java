package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;

public class QualifiedNameToken extends AbstractToken{


  private final Name _qualifier;

  public QualifiedNameToken(QualifiedName qualifiedName) {
    super(qualifiedName);
    _name = qualifiedName.getName().toString();
    _qualifier = qualifiedName.getQualifier();
    ITypeBinding type = qualifiedName.resolveTypeBinding();
    _type = type == null ? "Failed" : type.getName();
  }

  public Name getQualifier() {
    return _qualifier;
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws WrongPatchException {
    if(!(availableVars.contains(_name) || availableVars.contains(_qualifier.getFullyQualifiedName()))){
      throw new WrongPatchException();
    }
    return new Pair(_ast.newQualifiedName((Name) ASTNode.copySubtree(_ast,_qualifier),_ast.newSimpleName(_name)),1.0);
  }

  @Override
  public boolean equals(Object obj){
    if(!(obj instanceof QualifiedNameToken)){
      return false;
    }
    return _name.equals(((QualifiedNameToken)obj).getName()) && _qualifier.getFullyQualifiedName().equals(((QualifiedNameToken)obj).getQualifier().getFullyQualifiedName());
  }

  @Override
  public int hashCode(){
    return super.hashCode()+_qualifier.getFullyQualifiedName().hashCode();
  }

  @Override
  public boolean isLeafNode() {
    return true;
  }
}
