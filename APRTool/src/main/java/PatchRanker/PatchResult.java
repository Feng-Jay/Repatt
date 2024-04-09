package PatchRanker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatchResult {
  String patch;
  Map<String, Map<Integer, List<String>>> path2Patch;
  String name;

  public PatchResult(String patch, String name) {
    this.name = name;
    this.patch = patch;
    File patchFolder = new File(patch);
    path2Patch = new HashMap<>();
    for (File project: patchFolder.listFiles()){
      if (project.getName().equals(".DS_Store")) continue;
      for (File id : project.listFiles()){
        if (id.getName().equals(".DS_Store")) continue;
        for (File patchFile: id.listFiles()){
          if (patchFile.getName().endsWith(".patch")){
            String path = patchFile.getAbsolutePath();
            path2Patch.putIfAbsent(project.getName(), new HashMap<>());
            path2Patch.get(project.getName()).putIfAbsent(Integer.parseInt(id.getName()), new ArrayList<>());
            path2Patch.get(project.getName()).get(Integer.parseInt(id.getName())).add(path);
          }
        }
      }
    }
  }
}
