package cofix.core.preprocess.statementRepair;


import cofix.common.util.JavaFile;
import cofix.core.pattern.MatchedPattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ShowInfo {

  private static FragmentProcessor processor;

  public ShowInfo(FragmentProcessor processor) {
    ShowInfo.processor = processor;
  }

  public void showFrequent() {
    StringBuilder frequent = new StringBuilder();
    for (Entry<Integer, String> entry : processor.getFrequenciesName().getKv().entrySet()) {
      frequent.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
    }
    JavaFile.writeStringToFile("/Users/yezizhi/Desktop/log/frequent.txt", frequent.toString());
  }

  public void showPatterns(List<MatchedPattern> patterns) {
    //print all patterns
    StringBuilder sbPattern = new StringBuilder();
    int cnt=0;
    for (MatchedPattern pattern : patterns) {
      cnt++;
      sbPattern.append("This is ").append(cnt).append(" =======PatternStart=====\n");
      for (Integer id : pattern.getPatterns()) {
        if ( processor.getFrequenciesName().containsKey(id)) {
          sbPattern.append(id).append(":").append( processor.getFrequenciesName().getValue(id)).append("\n");
        }
      }
    }
    JavaFile.writeStringToFile("/Users/yezizhi/Desktop/log/pattern.txt", sbPattern.toString());

  }




  public void showMatchedPattern(Set<Map<Integer, List<Integer>>> matchedPatterns) {
    StringBuilder sbMatch = new StringBuilder();
    // Set<Map<Integer, List<Integer>>> expected = new HashSet<>();
    int k = 0;
    boolean test=false;
    if(test){

    }

    for (Map<Integer, List<Integer>> map : matchedPatterns) {
      Map<Integer, List<Integer>> temp = new HashMap<>();
      k++;
      sbMatch.append("This is ").append(k).append(" =======PatternStart=====\n");
      for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
        // List<Integer> tempp = new LinkedList<>();
        for (Integer i : entry.getValue()) {
          if ( processor.getFrequenciesName().containsKey(i)) {
            sbMatch.append(i).append(":").append( processor.getFrequenciesName().getValue(i)).append(" insert in : ")
                .append(entry.getKey()).append("\n");
            // System.out.println(_frequenciesName.getValue(i) + " insert in : " + entry.getKey());
//              if (i.equals(13892)) {
//                tempp.add(i);
//              }
          }
        }
//          if (!tempp.isEmpty()) {
//            temp.put(entry.getKey(), tempp);
//          }

      }

      //  System.out.println("=======matchedPatternEnd=====" + k);
//        if (!temp.isEmpty()) {
//          expected.add(temp);
//        }
      JavaFile.writeStringToFile("/Users/yezizhi/Desktop/log/matchPattern.txt", sbMatch.toString());
    }
  }

  public void showAllSequences() {
    StringBuilder buf = new StringBuilder();
    for (List<Integer> list : processor.getAllSequences()) {
      for (Integer x : list) {
        buf.append(processor.getFrequenciesName().getKv().get(x)).append("(").append(x).append(")")
            .append(" ");
      }
      buf.append("\n=====================\n");
    }
    JavaFile.writeStringToFile("/Users/yezizhi/Desktop/log/allSequence.txt", buf.toString());
  }

}
