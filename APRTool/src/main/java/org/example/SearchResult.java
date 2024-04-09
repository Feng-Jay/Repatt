package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonAppend.Attr;
import com.github.javaparser.ast.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchResult {
  public String project;
  public String branch;

  @JsonProperty
  List<String> stmts2search;

  @JsonProperty
  Map<String, Integer> fragLength;

  public SearchResult(String project, String branch, Set<Node> stmts2search, Map<Node, Integer> fragLength) {
    this.project = project;
    this.branch = branch;
    this.stmts2search = new ArrayList<>();
    for (Node node : stmts2search) {
      this.stmts2search.add(node.toString());
    }
    this.fragLength = new HashMap<>();
    for (Node node : fragLength.keySet()) {
      this.fragLength.put(node.toString(), fragLength.get(node));
    }
  }
}
