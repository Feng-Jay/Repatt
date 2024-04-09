package cofix.core;

import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.match.CodeBlockMatcher;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.search.CodeSearch;
import cofix.core.preprocess.tokenRepair.FileLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Divider {

  private static final double MIN_SIMILARITY = 0.99;

  Map<String,Double> similarityMap = new HashMap<>();

  private void buildSimilarityMap(Subject subject) {
    AbstractFaultlocalization fl = subject.getAbstractFaultlocalization();
    List<Pair<String, Integer>> locations = fl.getLocations(200);
    for (Pair<String, Integer> location : locations) {
      for (Pair<String, Integer> stringIntegerPair : locations) {
        if(location == stringIntegerPair) continue;
        double similarity = getSimilarity(subject,location,stringIntegerPair);
        similarityMap.put(location.getFirst()+location.getSecond().toString()+'|'+stringIntegerPair.getFirst()+stringIntegerPair.getSecond().toString(),similarity);
        similarityMap.put(stringIntegerPair.getFirst()+stringIntegerPair.getSecond().toString()+'|'+location.getFirst()+location.getSecond().toString(),similarity);
      }
    }
  }
  public List<Pair<String, Integer>> getSimilarLocations(Subject subject, Pair<String, Integer> oneLocation) {
    if(similarityMap.isEmpty()){buildSimilarityMap(subject);}
    AbstractFaultlocalization fl = subject.getAbstractFaultlocalization();
    List<Pair<String, Integer>> result = new ArrayList<>(), locations = fl.getLocations(200);
    for (Pair<String, Integer> location : locations) {
      if(oneLocation.equals(location)) continue;
      if(similarityMap.get(location.getFirst()+location.getSecond().toString()+'|'+oneLocation.getFirst()+oneLocation.getSecond()) > MIN_SIMILARITY){
        result.add(location);
      }
    }
    return result;
  }

  public static List<List<Pair<String, Integer>>> divide(Subject subject) {
    AbstractFaultlocalization fl = subject.getAbstractFaultlocalization();
    List<Pair<String, Integer>> locations = fl.getLocations(200);

    List<List<Pair<String, Integer>>> result = new ArrayList<>();

    while (!locations.isEmpty()) {
      List<Pair<String, Integer>> current = new ArrayList<>();
      Pair<String, Integer> target = locations.get(0);
      locations.remove(0);
      current.add(target);
      for (Iterator<Pair<String, Integer>> i = locations.iterator(); i.hasNext(); ) {
        Pair<String, Integer> p = i.next();
        if (getSimilarity(subject, target, p) > MIN_SIMILARITY) {
          //判断是否同一块
          boolean isSame=false;
          for(Pair<String,Integer> temp: current){
            if (p.getFirst().equals(temp.getFirst()) && p.getSecond() - temp.getSecond() < 5) {
              isSame = true;
              break;
            }
          }
          if (!isSame) {
            current.add(p);
            i.remove();
          }
        }
      }
      if(current.size()<2)continue;
      result.add(current);
    }

    return result;
  }

  private static double getSimilarity(Subject sub, Pair<String, Integer> p1,
      Pair<String, Integer> p2) {
    CompilationUnit cu1, cu2;
    String filePath1 = JavaFile.class2Path(sub, p1.getFirst());
    String filePath2 = JavaFile.class2Path(sub, p2.getFirst());
    FileLoader fl1 = sub.getFileLoader();

    if (p1.getFirst().equals(p2.getFirst())) {
      cu1 = cu2 = fl1.getCompilationUnit(filePath1);
    } else {
      cu1 = fl1.getCompilationUnit(filePath1);
      cu2 = fl1.getCompilationUnit(filePath2);
    }

    CodeSearch codeSearch1 = new CodeSearch(cu1, p1.getSecond(), 2, null, 3);
    CodeSearch codeSearch2 = new CodeSearch(cu2, p2.getSecond(), 2, null, 3);

    CodeBlock codeBlock1 = new CodeBlock(filePath1, cu1, codeSearch1.getASTNodes());
    CodeBlock codeBlock2 = new CodeBlock(filePath2, cu2, codeSearch2.getASTNodes());

    return CodeBlockMatcher.getSimilarity(codeBlock1, codeBlock2);
  }
}
