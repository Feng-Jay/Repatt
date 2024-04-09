package cofix.core.modification;

import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.metric.SVariable;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.search.CodeSearch;
import cofix.core.preprocess.BugFinder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

public class BuggyFile {

  private final String _filePath;

  private static final HashMap<String, BuggyFile> _fileCache = new HashMap<>();
  private Document _document;
  private final Subject _subject;
  private UndoEdit _undoEdit = null;
  private final String _fileBackUp;
  private final CompilationUnit _cu;

  public BuggyFile(Subject subject, Pair<String, Integer> location) {
    _subject = subject;
    _filePath = JavaFile.class2Path(subject, location.getFirst());
    _document = new Document(JavaFile.readFileToString(_filePath));
    _cu = _subject.getFileLoader().getCompilationUnit(_filePath);
    _fileBackUp = _document.get();
  }

  public static BuggyFile getBuggyFile(Subject subject, Pair<String, Integer> location) {
    String filePath = JavaFile.class2Path(subject, location.getFirst());
    if (_fileCache.containsKey(filePath)) {
      return _fileCache.get(filePath);
    } else {
      BuggyFile buggyFile = new BuggyFile(subject, location);
      _fileCache.put(filePath, buggyFile);
      return buggyFile;
    }
  }

  public ASTNode getStatement(int line) {
    BugFinder bugfinder = new BugFinder(line, _cu);
    _cu.accept(bugfinder);

    return bugfinder.getBugStatment();
  }

  public BuggyFile(String filepath, int lineNumber) {
    _filePath = filepath;
    _document = new Document(JavaFile.readFileToString(filepath));
    _fileBackUp = _document.get();
    _cu = JavaFile.genASTFromFileWithType(_filePath,
        filepath.substring(0, filepath.lastIndexOf("/")));
    _subject = null;
  }

  public CompilationUnit getCompilationUnit() {
    return _cu;
  }

  public Subject getSubject() {
    return _subject;
  }

  public Document getDocument() {
    return _document;
  }

  public String getFilePath() {
    return _filePath;
  }

  public void restoreFile() {
    JavaFile.writeStringToFile(_filePath, _fileBackUp);
    _document = new Document(JavaFile.readFileToString(_filePath));
  }

  public void reloadFile() {
    _document = new Document(JavaFile.readFileToString(_filePath));
  }

  public String applyEdit(TextEdit edit) {
    try {
      StringBuilder sb = new StringBuilder();
      _undoEdit = edit.apply(_document);
      IRegion beginLine = _document.getLineInformationOfOffset(edit.getRegion().getOffset());
      IRegion endLine = _document.getLineInformationOfOffset(
          edit.getRegion().getOffset() + edit.getRegion().getLength() + 1);
      sb.append(_document.get(), beginLine.getOffset(), endLine.getOffset() + endLine.getLength())
          .append("\n")
          .append("============================\n");
      return sb.toString();
      //JavaFile.writeStringToFile(_filePath, _document.get());
    } catch (BadLocationException e) {
      throw new RuntimeException("Illegal Edition!!");
    }
  }

  public Set<String> getAvailableVars(int line) {
    Set<String> result = new HashSet<>();
    for (Entry<String, Type> vars : NodeUtils.getUsableVarTypes(_filePath, line).entrySet()) {
      result.add(vars.getKey());
    }
    return result;
  }


}
