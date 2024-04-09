package cofix.core.preprocess.tokenRepair;

import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.modification.BuggyFile;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class FileLoader {
  private Map<String, CompilationUnit> fileMap = new HashMap<>();
  private Subject _subject;

  public FileLoader(Subject subject) {
    _subject = subject;
  }

  public CompilationUnit getCompilationUnit(String filepath) {
    if (fileMap.containsKey(filepath)) {
      return fileMap.get(filepath);
    }
    CompilationUnit cu =
        JavaFile.genASTFromFileWithType(filepath, _subject.getHome()+_subject.getSsrc(),_subject.getHome()+"/lib");
    fileMap.put(filepath, cu);
    return cu;
  }
}
