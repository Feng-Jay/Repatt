package org.example;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import java.util.List;

class FileToolsTest {

  public static void main(String[] args) {
    Expression target = StaticJavaParser.parseExpression("a");
    Statement stmt = StaticJavaParser.parseStatement("a = a + 1;");
    System.out.printf("Target: %s\n", target);

  }

}