package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.utils.ProjectRoot;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.tongfei.progressbar.ProgressBar;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;

public class Main {

  //static String path2dot = "/Users/higgs/bugs-dot-jar";
  static String path2dot = "/Users/higgs/PycharmProjects/gumtree/d4j";
  static String path2diffs = "/Users/higgs/PycharmProjects/gumtree/d4jdata";
  //static String projects[] = new String[]{"accumulo","camel","commons-math","flink", "jackrabbit-oak", "logging-log4j2","maven","wicket"};
  static String projects[] = new String[]{"Chart", "Cli", "Closure", "Codec", "Collections", "Compress", "Csv", "Gson", "JacksonCore", "JacksonDatabind", "JacksonXml", "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"};


  public static void main(String[] args) throws FileNotFoundException {
    StaticJavaParser.getParserConfiguration().setAttributeComments(false);
    File diffsFolder = new File(path2diffs);
    ObjectMapper objectMapper = new ObjectMapper();
    int success = 0;
    int failed = 0;
    for (String project : projects){
      File projectDiffFolder = new File(diffsFolder, project);
      File[] bugFolders = Arrays.stream(projectDiffFolder.listFiles()).filter(File::isDirectory).toArray(File[]::new);
      for(File bugFolder: ProgressBar.wrap(Arrays.asList(bugFolders), "Processing " + project)){
        try {
          String branchName = FileTools.readFile2String(new File(bugFolder, "meta.txt"));
          CompilationUnit before = StaticJavaParser.parse(new File(bugFolder, "before.java"));
          CompilationUnit after = StaticJavaParser.parse(new File(bugFolder, "after.java"));
          ProcResult result = new ProcBuilder("git")
              .withArgs("checkout", "-f", branchName)
              .withWorkingDirectory(new File(path2dot, project + "_" + bugFolder.getName()))
              .run();
          Project repo = new Project(new File(path2dot, project + "_" + bugFolder.getName()).getAbsolutePath(), LanguageLevel.JAVA_11);
          Set<Node> nodes = new HashSet<>(ASTTools.getUpdatedStmts(before, after));
          Map<Node, Integer> searchResult = ASTTools.searchInCU(repo.getCompilationUnits(), nodes);
          if (searchResult == null) {
            failed++;
            System.out.println("Success: " + success + " Failed: " + failed);
            continue;
          }
          SearchResult searchResult1 = new SearchResult(project, branchName, nodes, searchResult);
          objectMapper.writeValue(new File(bugFolder, "searchResult.json"), searchResult1);
          success++;
        } catch (RuntimeException | IOException e) {
          failed++;
          System.out.println("Error in " + bugFolder.getAbsolutePath());
        }
      }
    }
  }
}
