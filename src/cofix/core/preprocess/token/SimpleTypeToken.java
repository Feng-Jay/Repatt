package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class SimpleTypeToken extends AbstractToken{
  private Name _typeName;
  public SimpleTypeToken(SimpleType simpleType) {
    super(simpleType);
    _name = simpleType.getName().getFullyQualifiedName();
    _typeName = simpleType.getName();
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws WrongPatchException{
    List oriArguments;
    if (origin instanceof MethodInvocation){
      oriArguments = ((MethodInvocation)origin).arguments();
    }else if (origin.getParent() instanceof ClassInstanceCreation){
      oriArguments = ((ClassInstanceCreation)origin.getParent()).arguments();
    }else if (origin.getParent() instanceof VariableDeclarationStatement){
      VariableDeclarationStatement vds = (VariableDeclarationStatement) ASTNode.copySubtree(_ast, (VariableDeclarationStatement)origin.getParent());
      if(vds.getType() instanceof ParameterizedType){
        ParameterizedType pt = (ParameterizedType) vds.getType();
        pt.typeArguments().add(_ast.newSimpleType((Name)ASTNode.copySubtree(_ast, _typeName)));
        vds.setType(pt);
      }else{
        vds.setType(_ast.newSimpleType((Name)ASTNode.copySubtree(_ast, _typeName)));
      }
      return new Pair(vds,1.0);
    }else {
      throw new WrongPatchException();
    }
    ClassInstanceCreation target = _ast.newClassInstanceCreation();
    target.setType(_ast.newSimpleType((Name)ASTNode.copySubtree(_ast,_typeName)));
    for(Object o: oriArguments){
      target.arguments().add(ASTNode.copySubtree(_ast,(ASTNode) o));
    }
    return new Pair(target,1.0);
  }
}
