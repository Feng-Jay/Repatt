package PatchRanker;

import java.io.File;
import org.buildobjects.process.ExternalProcessFailureException;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;
import org.example.FileTools;
import org.example.Pair;

public class Patch {
  public String ProjectPath;
  public String PatchPath;

  public Patch(String PatchPath, String ProjectPath) {
    this.PatchPath = PatchPath;
    this.ProjectPath = ProjectPath;
  }

  public Pair<String, String> apply() {
    new ProcBuilder("git").withWorkingDirectory(new File(this.ProjectPath)).withArgs("reset", "--hard").run();
    ProcResult applyResult = null;
    try {
      applyResult = new ProcBuilder("git").withWorkingDirectory(new File(this.ProjectPath))
          .withArgs("apply", this.PatchPath).run();
    } catch (ExternalProcessFailureException e) {
      applyResult = new ProcBuilder("git").withWorkingDirectory(new File(this.ProjectPath))
          .withArgs("apply","--recount","--ignore-space-change", this.PatchPath).run();
    }
    if (applyResult.getExitValue() != 0) {
      throw new RuntimeException("Patch apply failed");
    }
    String modifiedPath = new ProcBuilder("bash").withArgs("-c","git status --porcelain | awk 'match($1, \"M\"){print $2}'").withWorkingDirectory(new File(this.ProjectPath)).run().getOutputString().split("\n")[0];
    String afterPatch = FileTools.readFile2String(new File(this.ProjectPath, modifiedPath));
    new ProcBuilder("git").withWorkingDirectory(new File(this.ProjectPath)).withArgs("reset", "--hard").run();
    String beforePatch = FileTools.readFile2String(new File(this.ProjectPath, modifiedPath));
    return new Pair<>(beforePatch,afterPatch);
  }

}
