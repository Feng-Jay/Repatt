package cofix.core.preprocess.tokenRepair;

import cofix.common.config.Constant;
import cofix.common.run.Executor;
import cofix.common.util.Subject;
import cofix.core.pattern.MatchedPattern;
import cofix.core.preprocess.Patch;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class PatchFilter {

  Statement _buggyStatement = null;

  public PatchFilter(ASTNode buggyNode) {
    while (!(buggyNode instanceof Statement)) {
      buggyNode = buggyNode.getParent();
      if (buggyNode == null) {
        return;
      }
    }
    _buggyStatement = (Statement) buggyNode;
  }

  public void filt(List<Patch> patches, TokensIdentifier identifier, Subject sub, String location)
      throws IOException {
    location = location.replace(".", "_");
    Set<Statement> allPossibleStatement = new HashSet<>();
    Map<String, Integer> StmtMap = new HashMap<>();
    Integer id = 0;
    if (_buggyStatement == null) {
      return;
    }
    MethodDeclaration fakeMethod = _buggyStatement.getAST().newMethodDeclaration();
    fakeMethod.setBody(_buggyStatement.getAST().newBlock());
    fakeMethod.getBody().statements()
        .add((Statement) ASTNode.copySubtree(_buggyStatement.getAST(), _buggyStatement));
    StmtMap.put(_buggyStatement.toString(), id);
    String filePath = "similarCode/" + sub.getName() + "/" + sub.getId();
    File similarCsv = new File(filePath + "/" + location + "_similar.csv");
    if (!similarCsv.exists()) {
      similarCsv.getParentFile().mkdirs();
      similarCsv.createNewFile();
    }
    CSVWriter writer = (CSVWriter) new CSVWriterBuilder(
        new FileWriter(similarCsv))
        .withSeparator('\t')
        .build();
    writer.writeNext(new String[]{id.toString(), fakeMethod.toString()});
    id++;
    patches.forEach(i -> i.getMatchedPatterns()
        .forEach(p -> allPossibleStatement.addAll(p.commonStatement(identifier))));
    for (Statement stmt : allPossibleStatement) {
      if (StmtMap.containsKey(stmt.toString())) {
        continue;
      }
      StmtMap.put(stmt.toString(), id);
      fakeMethod = _buggyStatement.getAST().newMethodDeclaration();
      fakeMethod.setBody(_buggyStatement.getAST().newBlock());
      fakeMethod.getBody().statements()
          .add((Statement) ASTNode.copySubtree(_buggyStatement.getAST(), stmt));
      writer.writeNext(new String[]{id.toString(), fakeMethod.toString()});
      id++;
    }
    writer.close();
    Map<Integer, Double> similarityMap = new HashMap<>();
    List<String> result = Executor.execute(new String[]{"/bin/bash", "-c",
        "cd " + Constant.PATH_TO_ASTNN + " && " + Constant.PATH_TO_PYTHON
            + " predict.py"
            + " --input " + similarCsv.getAbsolutePath()
            + " --output " + similarCsv.getParentFile().getAbsolutePath() + "/" + location
            + "_result.txt"});
    System.out.println(result);
    File resultFile = new File(filePath + "/" + location + "_result.txt");
    //read file
    BufferedReader br = new BufferedReader(
        new InputStreamReader(resultFile.toURI().toURL().openStream()));
    String line;
    while ((line = br.readLine()) != null) {
      String[] split = line.split(" ");
      similarityMap.put(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
    }
    br.close();

    Iterator<Patch> iterator = patches.iterator();
    File patternStmt = new File(filePath + "/" + location + "_patt.csv");
    if (!patternStmt.exists()) {
      patternStmt.getParentFile().mkdirs();
      patternStmt.createNewFile();
    }
    writer = (CSVWriter) new CSVWriterBuilder(
        new FileWriter(patternStmt))
        .withSeparator('\t')
        .build();
    while (iterator.hasNext()) {
      Patch patch = iterator.next();
      double max = 0;
      Integer maxBlockId = -1;
      for (MatchedPattern pattern : patch.getMatchedPatterns()) {
        if (pattern == MatchedPattern.mutatePattern) {
          max = 1;
          break;
        }
        Set<String> writtenStmt = new HashSet<>();
        for (Statement stmt : pattern.commonStatement(identifier)) {
          if (!writtenStmt.contains(stmt.toString())) {
            writer.writeNext(new String[]{pattern.toString(identifier), stmt.toString()});
            writtenStmt.add(stmt.toString());
          }
          Integer stmtId = StmtMap.get(stmt);
          if (!similarityMap.containsKey(stmtId)) {
            max = 1;
            maxBlockId = stmtId;
          } else {
            if (similarityMap.get(stmtId) > max) {
              max = similarityMap.get(stmtId);
              maxBlockId = stmtId;
            }
          }
        }
      }
      if (max < 0.9) {
        iterator.remove();
      }
      patch.setSimilarity(max);
      patch.setBlockId(maxBlockId);
    }
    writer.close();
  }
}

