package cofix.common.util;

import java.util.HashSet;
import java.util.Set;

public class SetTool {

  //get intersection of two set
  public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
    Set<T> result = new HashSet<>(set1);
    result.retainAll(set2);
    return result;
  }

}
