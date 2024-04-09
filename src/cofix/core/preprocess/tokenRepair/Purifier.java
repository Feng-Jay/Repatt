package cofix.core.preprocess.tokenRepair;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class Purifier {

  public static Boolean isValid(ASTNode originalNode, MethodInvocation mi,
      Set<IMethodBinding> methodBindings) {
    if (methodBindings == null) {
      return true;
    }
    Map<String, ITypeBinding> typeMap = new HashMap<>();
    if ((originalNode instanceof MethodInvocation)) {
      for (Object o : ((MethodInvocation) originalNode).arguments()) {
        if (o instanceof Expression) {
          ITypeBinding typeBinding = ((Expression) o).resolveTypeBinding();
          typeMap.put(o.toString(), typeBinding);
        }
      }
    }
    int argCount = mi.arguments().size();
    LOOP:
    for (IMethodBinding methodBinding : methodBindings) {
      List<ITypeBinding> parameterTypes = Arrays.asList(methodBinding.getParameterTypes());
      if (methodBinding.getParameterTypes().length != argCount) {
        continue;
      }
      for (int i = 0; i < argCount; i++) {
        ITypeBinding typeBinding = parameterTypes.get(i);
        String arg = mi.arguments().get(i).toString();
        if (typeMap.containsKey(arg) && typeMap.get(arg) != null) {
          ITypeBinding argTypeBinding = typeMap.get(arg);
          if (!argTypeBinding.isAssignmentCompatible(typeBinding)) {
            continue LOOP;
          }
        }
      }
      return true;
    }
    return false;
  }

  public static boolean checkInfixExpression(ASTNode node) {
    InfixExpression expr;
    if (node instanceof InfixExpression) {
      expr = (InfixExpression) node;
    } else if (node.getParent() instanceof InfixExpression) {
      expr = (InfixExpression) node.getParent();
    } else {
      return true;
    }

    if (expr.getLeftOperand().toString().equals(expr.getRightOperand().toString())) {
      return false;
    }
    return true;
  }

}
