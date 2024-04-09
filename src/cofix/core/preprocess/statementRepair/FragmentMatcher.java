package cofix.core.preprocess.statementRepair;

import cofix.common.util.Pair;
import cofix.core.pattern.MatchedPattern;
import cofix.core.preprocess.pattern.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.ASTNode;

public class FragmentMatcher {


  private final List<Integer> _numberPresentation;


  public FragmentMatcher(ASTNode node, List<Integer> number) {
    _numberPresentation = number;
  }


  public Map<Integer, List<Integer>> tryInsert(MatchedPattern inputPattern,
      Integer bugSize, Map<Integer, Fragment> frequencies) {
    Integer head = -1;
    Integer rear = -1;
    List<Pair<Integer, Integer>> boundary = new ArrayList<>();
    //get boundary
    //匹配上且不是占位符的集合
    //boundary first : index in pattern second:index in bug
    List<Integer> pattern = inputPattern.getPatterns();
    int[] match = inputPattern.getMatch();
    for (int index = 0; index < pattern.size(); index++) {
      if (match[index] != -1) {
        Pair<Integer, Integer> pair = new Pair<>(index, match[index]);
        //占位符是无效的匹配，信息量太小
        if (pattern.get(index) != -3) {
          boundary.add(pair);
        }
      }
    }
    Map<Integer, List<Integer>> insert = new HashMap<>();

    if (boundary.size() != 0) {
      //head rear in bug index
      for (int i = 0; i < boundary.size(); i++) {
        int pos = boundary.get(i).getSecond();
        //get gap
        if (i == 0) {
          head = 0;
        } else {
          head = boundary.get(i - 1).getSecond();
        }
        if (i == boundary.size() - 1) {
          rear = bugSize - 1;
        } else {
          rear = boundary.get(i + 1).getSecond();
        }
        if (head > rear) {
          int temp = head;
          head = rear;
          rear = temp;
        }
        if (boundary.get(i).getFirst() == pattern.size() - 1) {
          //无法后插
          rear = -1;
        }
        if (boundary.get(i).getFirst() == 0) {
          //无法前插
          head = -1;
        }
        //insert before:head & pos
        if (head != -1) {
          for (int patternPos = 0; patternPos < boundary.get(i).getFirst(); patternPos++) {
            for (int indexOfBug = head; indexOfBug <= pos; indexOfBug++) {
              if (insert.containsKey(indexOfBug)) {
                insert.get(indexOfBug).add(pattern.get(patternPos));
              } else {
                List<Integer> candidatePatterns = new ArrayList<>();
                //Try to insert one by one after the rear
                candidatePatterns.add(patternPos);
                insert.put(indexOfBug, candidatePatterns);
              }
            }
          }
        }
        //insert after: pos & rear
        if (rear != -1) {
          for (int patternPos = boundary.get(i).getFirst() + 1; patternPos <= pattern.size() - 1;
              patternPos++) {
            for (int indexOfBug = pos; indexOfBug <= rear; indexOfBug++) {
              if (insert.containsKey(indexOfBug)) {
                insert.get(indexOfBug).add(pattern.get(patternPos));
              } else {
                List<Integer> candidatePatterns = new ArrayList<>();
                //Try to insert one by one after the rear
                candidatePatterns.add(pattern.get(patternPos));
                insert.put(indexOfBug, candidatePatterns);
              }
            }
          }
        }
      }
    }

    return insert;
  }
}
