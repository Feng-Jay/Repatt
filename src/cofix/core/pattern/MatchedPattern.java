package cofix.core.pattern;

import cofix.core.preprocess.token.IdentifierToken;
import cofix.core.preprocess.tokenRepair.TokensIdentifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.Statement;

public class MatchedPattern extends pattern implements Comparable {

  private final List<Integer> _patterns;

  public static final MatchedPattern mutatePattern = new MatchedPattern(null, null, 100);
  private final int[] _match;
  private final int _frequency;
  private Map<Integer, List<Integer>> _fixMap = null;

  public MatchedPattern(List<Integer> patterns, int[] match, int frequency) {
    _patterns = patterns;
    _match = match;
    _frequency = frequency;
  }

  public Set<Statement> commonStatement(TokensIdentifier identifier) {
    Set<Statement> result = new HashSet<>();
    if (this == MatchedPattern.mutatePattern) {
      return result;
    }
    for (int i = 0; i < _patterns.size(); i++) {
      if (identifier.getToken(_patterns.get(i)) instanceof IdentifierToken) {
        continue;
      }
      if (result.isEmpty()) {
        result.addAll(identifier.getToken(_patterns.get(i)).resolveOccurrenceStmt());
      } else {
        result.retainAll(identifier.getToken(_patterns.get(i)).resolveOccurrenceStmt());
      }
    }
    return result;
  }

  public void setFixMap(Map<Integer, List<Integer>> fixMap) {
    _fixMap = fixMap;
  }

  public Map<Integer, List<Integer>> getFixMap() {
    return _fixMap;
  }

  public int getFrequency() {
    return _frequency;
  }

  public List<Integer> getPatterns() {
    return _patterns;
  }

  public int[] getMatch() {
    return _match;
  }

  public String toString(TokensIdentifier identifier) {
    if (_patterns == null) {
      return "mutatePattern";
    }
    StringBuilder sb = new StringBuilder();
    for (Integer p : _patterns) {
      sb.append(identifier.getToken(p).getName() + " ");
    }
    return sb.toString();
  }

  @Override
  public int compareTo(Object o) {
    MatchedPattern obj = (MatchedPattern) o;
    if (_frequency != obj._frequency) {
      return obj._frequency - _frequency;
    } else {
      return 0;
    }

  }
}

