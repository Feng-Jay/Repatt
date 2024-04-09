package PatchRanker;

import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import java.io.File;
import java.io.IOException;
import org.example.FileTools;
import org.example.Pair;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

public class GumtreeGrader extends AbstractGrader{

  @Override
  public double grade(Pair<String, String> p) {
    double score = 0;
    AstComparator comparator = new AstComparator();
    Diff compare = comparator.compare(p.first, p.second);
    score = compare.getAllOperations().size();
    return score;
  }

}
