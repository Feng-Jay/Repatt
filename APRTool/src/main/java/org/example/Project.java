package org.example;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.tongfei.progressbar.ProgressBar;

public class Project {

  List<CompilationUnit> cus = new ArrayList<>();

  String path;
  ProjectRoot projectRoot;
  public Project(String path, LanguageLevel languageLevel){
    this.path = path;
    ParserCollectionStrategy parser = new ParserCollectionStrategy();
    parser.getParserConfiguration().setAttributeComments(false);
    parser.getParserConfiguration().setLanguageLevel(languageLevel);
    List<SourceRoot> sourceRoots = parser.collect(new File(path).toPath()).getSourceRoots();
    for (SourceRoot sourceRoot : sourceRoots) {
      try {
        sourceRoot.tryToParse();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      cus.addAll(sourceRoot.getCompilationUnits());
    }
  }

  public List<CompilationUnit> getCompilationUnits(){
    return cus;
  }



}
