package PatchRanker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.tongfei.progressbar.ProgressBar;
import org.example.FileTools;
import org.example.Pair;

public class PatchRanker {
  public Map<String, Map<Integer, List<Pair<String, List<String>>>>> patchPaths = new HashMap<>();
  public Map<String, Map<Integer, List<Pair<String, List<Patch>>>>> patches = new HashMap<>();

  public Map<String, Map<Integer, List<Patch>>> sortedPatches = new HashMap<>();


  public PatchRanker() {
  }

  public void addPatchResult(PatchResult patchResult) {
    for (String project : patchResult.path2Patch.keySet()) {
      patchPaths.putIfAbsent(project, new HashMap<>());
      sortedPatches.putIfAbsent(project, new HashMap<>());
      for (Integer id : patchResult.path2Patch.get(project).keySet()) {
        patchPaths.get(project).putIfAbsent(id, new ArrayList<>());
        sortedPatches.get(project).putIfAbsent(id, new ArrayList<>());
        patchPaths.get(project).get(id).add(new Pair<>(patchResult.name, patchResult.path2Patch.get(project).get(id)));
      }
    }
  }

  public void anchorPatch(String d4j_path){
    for(String project : patchPaths.keySet()){
      patches.putIfAbsent(project, new HashMap<>());
      for(Integer id : patchPaths.get(project).keySet()){
        patches.get(project).putIfAbsent(id, new ArrayList<>());
        List<Pair<String, List<String>>> patchList = patchPaths.get(project).get(id);
        for(int i = 0; i < patchList.size(); i++){
          Pair<String, List<String>> patch = patchList.get(i);
          patches.get(project).get(id).add(new Pair<>(patch.first, new ArrayList<>()));
          for(String path : patch.second){
            patches.get(project).get(id).get(i).second.add(new Patch(path,d4j_path.formatted(project, id)));
          }

        }
      }
    }
  }

  public void rank(AbstractGrader grader){
    for(String project : patches.keySet()){
      for(Integer id : ProgressBar.wrap(patches.get(project).keySet(), "Ranking %s".formatted(project))){
        List<Pair<String, List<Patch>>> patchList = patches.get(project).get(id);
        sortedPatches.get(project).get(id).addAll(rankList(patchList, grader, project, id));
      }
    }
  }

  public List<Patch> rankList(List<Pair<String, List<Patch>>> patchList, AbstractGrader grader, String proj, Integer id){
    List<Patch> allPatches = new ArrayList<>();
    for(int i = 0; i < patchList.size(); i++){
      Pair<String, List<Patch>> patch = patchList.get(i);
      for(Patch p : patch.second){
        allPatches.add(p);
      }
    }
    Map<Patch, Double> scores = new HashMap<>();
    for (Patch patch : allPatches) {
      Pair<String, String> result = patch.apply();
      scores.put(patch, grader.grade(result));
    }
    if (proj.equals("Closure") && id.equals(62)){
      System.out.printf("");
    }
    allPatches.sort(Comparator.comparingDouble(scores::get));
    return allPatches;
  }

  public void saveResultTo(String path){
    StringBuilder sb = new StringBuilder();
    for (String project : sortedPatches.keySet()) {
      for (Integer id : sortedPatches.get(project).keySet()) {
        sb.append(project).append(" ").append(id).append("\n");
        for (Patch patch : sortedPatches.get(project).get(id)) {
          sb.append(patch.PatchPath).append("\n");
        }
        sb.append("===");
      }
    }
    FileTools.writeString2File(path, sb.toString());
  }

}
