package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PrimitiveType;

public class PrimitiveTypeToken extends AbstractToken{

  public PrimitiveTypeToken(PrimitiveType primitiveType) {
    super(primitiveType);
    _name = primitiveType.toString();
    _type = "primitiveType";
  }

  public PrimitiveTypeToken(String name) {
    _name = name;
    _type = "primitiveType";
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) {
    switch(_name){
      case "int":
        return new Pair(_ast.newPrimitiveType(PrimitiveType.INT),1.0);
      case "long":
        return new Pair(_ast.newPrimitiveType(PrimitiveType.LONG),1.0);
      case "float":
        return new Pair(_ast.newPrimitiveType(PrimitiveType.FLOAT),1.0);
      case "double":
        return new Pair(_ast.newPrimitiveType(PrimitiveType.DOUBLE),1.0);
      case "short":
        return new Pair(_ast.newPrimitiveType(PrimitiveType.SHORT),1.0);
      case "byte":
        return new Pair(_ast.newPrimitiveType(PrimitiveType.BYTE),1.0);
      case "char":
        return new Pair(_ast.newPrimitiveType(PrimitiveType.CHAR),1.0);
      case "boolean":
        return new Pair(_ast.newPrimitiveType(PrimitiveType.BOOLEAN),1.0);
      default:
        throw new IllegalArgumentException("illegal");
    }
  }

  @Override
  public List<AbstractToken> makeMutations() {
    if(_name.equals("int")){
      List<AbstractToken> result = new ArrayList<>();
      result.add(new PrimitiveTypeToken("long"));
      result.add(new PrimitiveTypeToken("double"));
      return result;
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PrimitiveTypeToken && ((PrimitiveTypeToken) o)._name.equals(_name);
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

}
