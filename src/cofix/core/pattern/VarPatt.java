package cofix.core.pattern;

import cofix.core.preprocess.pattern.ExpArg;
import cofix.core.preprocess.pattern.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class VarPatt extends pattern {
  private final Set<ExpArg> _variables;
  private final List<String> _relativeFunc = new ArrayList<>();

  public VarPatt(Set<ExpArg> var, Map<String, Function> funcList) {
    _variables = var;
    for (Entry<String, Function> func : funcList.entrySet()) {
      if (func.getValue().contains(var)) {
        _relativeFunc.add(func.getValue().getName());
        func.getValue().addVarPatt(this);
      }
    }
  }

  public List<String> getRelativeFunc() {
    return _relativeFunc;
  }

  public Set<ExpArg> getVariables() {
    return _variables;
  }

  public void print() {
    System.out.println(_variables + " used in Function: " + _relativeFunc);
  }
}
