package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import cofix.core.parser.NodeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class SimpleNameToken extends AbstractToken {

  static final HashSet<String> NUM_COMPATIBLE = new HashSet<>(
      Arrays.asList("int", "double", "float", "long", "short", "byte"));

  static final Logger LOGGER = Logger.getLogger(SimpleNameToken.class.getName());

  public enum Usage {
    Variable, Field, SuperField, BreakLabel, Method, SuperMethod
  }

  Usage _usage;

  private boolean parseUsage() {
    if (this._elementClass == Expression.class) {
      _usage = Usage.Variable;
      return true;
    } else if (this._nodeClass == VariableDeclarationFragment.class && this._propertyid.equals(
        "name")) {
      _usage = Usage.Variable;
      return true;
    } else if (this._nodeClass == QualifiedName.class && this._propertyid.equals("qualifier")) {
      _usage = Usage.Variable;
      return true;
    } else if (this._nodeClass == SingleVariableDeclaration.class && this._propertyid.equals(
        "name")) {
      _usage = Usage.Variable;
      return true;
    } else if (this._nodeClass == MethodInvocation.class && this._propertyid.equals("name")) {
      _usage = Usage.Method;
      return true;
    } else if (this._nodeClass == SuperMethodInvocation.class && this._propertyid.equals("name")) {
      _usage = Usage.SuperMethod;
      return true;
    } else if (this._nodeClass == FieldAccess.class && this._propertyid.equals("name")) {
      _usage = Usage.Field;
      return true;
    } else if (this._nodeClass == BreakStatement.class) {
      _usage = Usage.BreakLabel;
      return true;
    } else {
      _usage = Usage.Variable;
      return false;
    }
  }

  public SimpleNameToken(SimpleName simpleName) {
    super(simpleName);
    _name = simpleName.getFullyQualifiedName();
    ITypeBinding type = simpleName.resolveTypeBinding();
    _type = type == null ? "Failed" : type.getQualifiedName();
    if (!parseUsage()) {
      LOGGER.warning("Failed to parse usage of SimpleNameToken: " +
          simpleName.getLocationInParent().getId() + " " + simpleName.getLocationInParent()
          .getNodeClass() + " " + _elementClass);
    }
  }

  public SimpleNameToken(SimpleName simpleName, String path, CompilationUnit cu) {
    super(simpleName);
    _name = simpleName.getFullyQualifiedName();
    ITypeBinding type = simpleName.resolveTypeBinding();
    StructuralPropertyDescriptor spd = simpleName.getLocationInParent();
    if (type == null) {
      char c = _name.charAt(0);
      Map<String, org.eclipse.jdt.core.dom.Type> usableTypes = NodeUtils.getUsableVarTypes(path,
          cu.getLineNumber(simpleName.getStartPosition()));
      if (c <= 90 && c >= 65) {
        _type = _name;
      } else if (usableTypes.containsKey(_name)) {
        _type = usableTypes.get(_name).toString();
      } else {
        _type = "Failed";
      }
    } else {
      _type = type.getQualifiedName();
    }
    if (!parseUsage()) {
      LOGGER.warning("Failed to parse usage of SimpleNameToken: " +
          simpleName.getLocationInParent().getId() + " " + simpleName.getLocationInParent()
          .getNodeClass() + " " + _elementClass);
    }
  }

  public SimpleNameToken(SimpleName simpleName, String path, CompilationUnit cu, Usage usage) {
    super(simpleName);
    _name = simpleName.getFullyQualifiedName();
    ITypeBinding type = simpleName.resolveTypeBinding();
    StructuralPropertyDescriptor spd = simpleName.getLocationInParent();
    _usage = usage;
    if (type == null) {
      char c = _name.charAt(0);
      Map<String, org.eclipse.jdt.core.dom.Type> usableTypes = NodeUtils.getUsableVarTypes(path,
          cu.getLineNumber(simpleName.getStartPosition()));
      if (c <= 90 && c >= 65) {
        _type = _name;
      } else if (usableTypes.containsKey(_name)) {
        _type = usableTypes.get(_name).toString();
      } else {
        _type = "Failed";
      }
    } else {
      _type = type.getQualifiedName();
    }
    if (!parseUsage()) {
      LOGGER.warning("Failed to parse usage of SimpleNameToken: " +
          simpleName.getLocationInParent().getId() + " " + simpleName.getLocationInParent()
          .getNodeClass() + " " + _elementClass);
    }
  }

  public ASTNode arbitraryBuild() {
    return _ast.newSimpleName(_name);
  }

  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars)
      throws WrongPatchException {
    if (_usage == Usage.Variable) {
      if (!availableVars.contains(_name)) {
        throw new WrongPatchException();
      }
      return new Pair<>(_ast.newSimpleName(_name), 1.0);
    } else if (_usage == Usage.Field) {
      if (!availableVars.contains(_name)) {
        throw new WrongPatchException();
      }
      FieldAccess fieldAccess = _ast.newFieldAccess();
      if (origin instanceof FieldAccess) {
        fieldAccess.setExpression(
            (Expression) ASTNode.copySubtree(_ast, ((FieldAccess) origin).getExpression()));
      } else {
        if (!availableVars.contains(_name)) {
          throw new WrongPatchException();
        }
        fieldAccess.setExpression(_ast.newThisExpression());
      }
      fieldAccess.setName(_ast.newSimpleName(_name));
      return new Pair(fieldAccess, 1.0);
    } else if (_usage == Usage.SuperField) {
      FieldAccess fieldAccess = _ast.newFieldAccess();
      fieldAccess.setExpression(_ast.newSuperFieldAccess());
      fieldAccess.setName(_ast.newSimpleName(_name));
      return new Pair(fieldAccess, 1.0);
    } else if (_usage == Usage.Method || _usage == Usage.SuperMethod) {
      if (origin.getLocationInParent().getId().equals("leftHandSide")) {
        throw new WrongPatchException();
      }
      List arguments = null;
      Expression expression = null;
      if (origin instanceof MethodInvocation) {
        arguments = ((MethodInvocation) origin).arguments();
        expression = ((MethodInvocation) origin).getExpression();
      } else if (origin instanceof SuperMethodInvocation) {
        arguments = ((SuperMethodInvocation) origin).arguments();
      } else if (origin instanceof ClassInstanceCreation) {
        arguments = ((ClassInstanceCreation) origin).arguments();
      } else {
        arguments = new ArrayList();
      }
      if (_usage == Usage.Method) {
        MethodInvocation methodInvocation = _ast.newMethodInvocation();
        if (expression != null) {
          methodInvocation.setExpression((Expression) ASTNode.copySubtree(_ast, expression));
        }
        methodInvocation.setName(_ast.newSimpleName(_name));
        for (Object argument : arguments) {
          methodInvocation.arguments().add(ASTNode.copySubtree(_ast, (ASTNode) argument));
        }
        return new Pair(methodInvocation, 1.0);
      } else if (_usage == Usage.SuperMethod) {
        SuperMethodInvocation superMethodInvocation = _ast.newSuperMethodInvocation();
        superMethodInvocation.setName(_ast.newSimpleName(_name));
        for (Object argument : arguments) {
          superMethodInvocation.arguments().add(ASTNode.copySubtree(_ast, (ASTNode) argument));
        }
        return new Pair(superMethodInvocation, 1.0);
      }
    } else if (_usage == Usage.BreakLabel) {
      return null;
    }
    throw new WrongPatchException();
  }


/*
  @Override
  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars)
      throws WrongPatchException {
    if (super._elementClass == Expression.class || origin.getLocationInParent() == null
        || super._nodeClass == VariableDeclarationFragment.class) {
      if (!availableVars.contains(_name)) {
        throw new WrongPatchException();
      }
      return new Pair(_ast.newSimpleName(_name), 1.0);
    } else {
      List oriArguments;
      Expression expression = null;
      if (origin.getLocationInParent().getId().equals("arguments")) {
        oriArguments = new ArrayList();
      } else if (origin instanceof MethodInvocation) {
        oriArguments = ((MethodInvocation) origin).arguments();
        expression = ((MethodInvocation) origin).getExpression();
      } else if (origin.getParent() instanceof ClassInstanceCreation) {
        oriArguments = ((ClassInstanceCreation) origin.getParent()).arguments();
      } else {
        oriArguments = new ArrayList();
      }
      if (super._nodeClass == MethodInvocation.class) {
        if (origin.getLocationInParent().getId().equals("leftHandSide")) {
          throw new WrongPatchException();
        }
        MethodInvocation target = _ast.newMethodInvocation();
        for (Object o : oriArguments) {
          target.arguments().add(ASTNode.copySubtree(_ast, (ASTNode) o));
        }
        target.setName(_ast.newSimpleName(_name));
        if (expression != null) {
          target.setExpression((Expression) ASTNode.copySubtree(_ast, expression));
        }
        return new Pair(target, 1.0);
      } else if (super._nodeClass == SuperMethodInvocation.class) {
        if (origin.getLocationInParent().getId().equals("leftHandSide")) {
          throw new WrongPatchException();
        }
        SuperMethodInvocation target = _ast.newSuperMethodInvocation();
        for (Object o : oriArguments) {
          target.arguments().add(ASTNode.copySubtree(_ast, (ASTNode) o));
        }
        target.setName(_ast.newSimpleName(_name));
        return new Pair(target, 1.0);
      } else if (super._nodeClass == BreakStatement.class) {
        return null;
      } else if (super._nodeClass == FieldAccess.class) {
        FieldAccess fieldAccess = _ast.newFieldAccess();
        if (origin instanceof FieldAccess) {
          if (!availableVars.contains(_name)) {
            throw new WrongPatchException();
          }
          fieldAccess.setExpression(
              (Expression) ASTNode.copySubtree(_ast, ((FieldAccess) origin).getExpression()));
        } else {
          if (!availableVars.contains(_name)) {
            throw new WrongPatchException();
          }
          fieldAccess.setExpression(_ast.newThisExpression());
        }
        fieldAccess.setName(_ast.newSimpleName(_name));
        return new Pair(fieldAccess, 1.0);
      }
      throw new RuntimeException();
    }
  }
*/

  @Override
  public boolean isCompatibleWith(AbstractToken token, Set<String> avaVars) {
    if (this._elementClass == Expression.class && !avaVars.contains(this._name)) {
      return false;
    }
    if (NUM_COMPATIBLE.contains(this._type)) {
      return NUM_COMPATIBLE.contains(token.getType());
    }
    return (_type.equals(token.getType()) || token instanceof NullLiteralToken);
  }

  @Override
  public boolean isLeafNode() {
    return true;
  }

  @Override
  public int hashCode() {
    if (_usage == null) {
      return _name.hashCode() + _type.hashCode();
    }
    return _name.hashCode() + _type.hashCode() + _usage.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SimpleNameToken) {
      SimpleNameToken token = (SimpleNameToken) obj;
      return _name.equals(token._name) && _type.equals(token._type) && _usage.equals(
          token._usage);
    }
    return false;
  }

}
