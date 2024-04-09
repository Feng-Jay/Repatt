package cofix.core.preprocess.pattern;

import cofix.common.util.DuoMap;
import cofix.core.pattern.VarPatt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class Function {

  public enum TYPE {
    UNKNOW,
    METHODINVOC,
    INSTANCECRE,
    CONSTRUCTORINOV
  }

  private static int idPool;
  private final int id;
  private final String _funcName;
  private final ASTNode _node;
  private final List<VarPatt> _relatedVarPatt = new ArrayList<>();
  private Set<List<ExpArg>> _oriArgList;
  private DuoMap<Integer, ExpArg> _argHash;
  private final TYPE _type;
  private String _returnType;

  public Function(ASTNode node, String name, String returnType) {
    _funcName = name;
    _oriArgList = new HashSet<>();
    id = idPool;
    idPool++;
    _node = node;
    _returnType = returnType;
    if (node instanceof MethodInvocation) {
      _type = TYPE.METHODINVOC;
    } else if (node instanceof ClassInstanceCreation) {
      _type = TYPE.INSTANCECRE;
    } else if (node instanceof ConstructorInvocation) {
      _type = TYPE.CONSTRUCTORINOV;
    } else {
      _type = TYPE.UNKNOW;
    }
  }

  public Function(ASTNode node, String name, Set<List<ExpArg>> argList, String returnType) {
    _funcName = name;
    _oriArgList = argList;
    id = idPool;
    idPool++;
    _node = node;
    _returnType = returnType;
    if (node instanceof MethodInvocation) {
      _type = TYPE.METHODINVOC;
    } else if (node instanceof ClassInstanceCreation) {
      _type = TYPE.INSTANCECRE;
    } else if (node instanceof ConstructorInvocation) {
      _type = TYPE.CONSTRUCTORINOV;
    } else {
      _type = TYPE.UNKNOW;
    }
  }

  public TYPE getType() {
    return _type;
  }

  public String getReturnType() {
    return _returnType;
  }

  public void setOriArgList(Set<List<ExpArg>> list) {
    _oriArgList = list;
  }

  public Set<List<ExpArg>> getOriArgList() {
    return _oriArgList;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return _funcName;
  }

  public void addArgSet(List<ExpArg> argSet) {
    _oriArgList.add(argSet);
  }

  public boolean contains(Set<ExpArg> set) {
    for (List<ExpArg> funcArg : _oriArgList) {
      if (funcArg.containsAll(set)) {
        return true;
      }
    }
    return false;
  }

  public void addVarPatt(VarPatt varPatt) {
    _relatedVarPatt.add(varPatt);
  }

  public List<VarPatt> getVarPatt() {
    return _relatedVarPatt;
  }

  @Override
  public String toString() {
    return "Function:" + _funcName;
  }
}
