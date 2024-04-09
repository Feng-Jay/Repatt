package cofix.core.preprocess.statementRepair;

import java.util.LinkedList;
import java.util.List;

public class SplitHelper {


  //遇到分支结构就申请一个新的helper
//和分层有关的变量 需要全局共享
  //共享头 比如condition 外层index就是level层级

  List<List<Integer>> _head = new LinkedList<>();//map level-head
  List<List<Integer>> _result = new LinkedList<>();
  public Integer _level = 0;


  public void stmtFork(List<Integer> head) {
    _head.add(head);
    _level++;

  }

  public void addCase(List<Integer>caseStmt,Integer level) {
    List<Integer>ans = new LinkedList<>();
    for(int i=0;i<_level;i++){
      ans.addAll(_head.get(i));
    }
    ans.addAll(caseStmt);
    _result.add(ans);
  }

  public List<List<Integer>> getResult() {
    return _result;
  }


  public List<List<Integer>> getHead() {
    return _head;
  }

  public Integer getLevel() {
    return _level;
  }


}



