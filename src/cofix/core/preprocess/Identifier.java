package cofix.core.preprocess;

import cofix.common.util.DuoMap;
import cofix.core.preprocess.pattern.Fragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.core.dom.ASTNode;

public class Identifier {

  private final DuoMap<Integer, Fragment> _idMap;//frequent stmt + expr
  private final DuoMap<Integer, String> _nameIdMap;
  private static int idPool = 0;
  private static int idFrequentPool = 0;
  private final DuoMap<Integer, Fragment> _frequencies = new DuoMap<>();// frequent stmt
  private final DuoMap<Integer, String> _frequenciesName = new DuoMap<>();
  private static List<Integer> _bugBlockSequence = new ArrayList<>();
  //private static List<List<Integer>> _allSequences = new ArrayList<>();

  public Identifier(
      DuoMap<Integer, Fragment> idMap, DuoMap<Integer, String> nameIdMap) {
    _idMap = idMap;
    _nameIdMap = nameIdMap;
  }

  public Integer setId(ASTNode node, String string) {
    if (_nameIdMap.containsValue(string) || _nameIdMap.containsValue(string + '\n')) {
      Integer id = _nameIdMap.getKey(string);
      Fragment fragment = _idMap.getValue(id);
      fragment.setTimes();
      return id;
    } else {
      _nameIdMap.put(idPool, string);
      Fragment fragment = new Fragment(node);
      fragment.setTimes();
      _idMap.put(idPool, fragment);
     return idPool++;
    }
  }

//  public boolean tokenFilter(ASTNode node) {
//    // boolean flag = false;
////    if (node instanceof NumberLiteral || node instanceof StringLiteral ||
////        node instanceof BooleanLiteral || node instanceof CharacterLiteral) {
////      return false;
////    }
//    if (
//        node.getParent() instanceof MethodInvocation && node.getLocationInParent().getId()
//            .equals("expression")
//            || node.getParent() instanceof ArrayAccess && node.getLocationInParent().getId()
//            .equals("array")
//            || node.getParent() instanceof IfStatement && node.getLocationInParent().getId()
//            .equals("expression")
//            || node.getParent() instanceof MethodInvocation && node.getLocationInParent().getId()
//            .equals("name")
//    ) {
//      return true;
//    } else {
//      return false;
//    }
//  }



  public void addNodeToBugSeq(ASTNode node) {
    if (_nameIdMap.containsValue(node.toString()) || _nameIdMap.containsValue(
        node.toString() + '\n')) {
      _bugBlockSequence.add(_nameIdMap.getKey(node.toString()));
    }
  }


  public void collectFrequent() {
    Map<Integer, Fragment> fragmentMap = _idMap.getKv();
    for (Entry<Integer, Fragment> entry : fragmentMap.entrySet()) {
      if (entry.getValue().getTimes() > 1) {
        _frequencies.put(idFrequentPool++, entry.getValue());
        _frequenciesName.put(idFrequentPool++, entry.getValue().getNodeType().toString());
      }
    }
    for (Entry<Integer, Fragment> entry : _frequencies.getKv().entrySet()) {
      _idMap.remove(entry.getKey(), entry.getValue());
    }
  }

  public Map<Integer, Fragment> getFrequentMap() {
    return _frequencies.getKv();
  }

  public Map<Integer, String> getFrequentNameMap() {
    return _frequenciesName.getKv();
  }

  public Map<Integer, Fragment> getIdMap() {
    return _idMap.getKv();
  }

}
