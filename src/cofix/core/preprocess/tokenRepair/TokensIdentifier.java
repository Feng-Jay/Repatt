package cofix.core.preprocess.tokenRepair;

import cofix.common.util.DuoMap;
import cofix.core.preprocess.token.AbstractToken;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;

public class TokensIdentifier {

  private final DuoMap<Integer, AbstractToken> _tokensMap = new DuoMap<>();
  private final HashMap<Integer, Integer> _tokensCount = new HashMap<>();
  private static int id = 0;

  public AbstractToken getToken(int id) {
    return _tokensMap.getValue(id);
  }

  public int getId(AbstractToken token) {
    if (_tokensMap.containsValue(token)) {
      return _tokensMap.getKey(token);
    } else {
      _tokensMap.put(id, token);
      _tokensCount.put(id, 0);
      return id++;
    }
  }

  public void addCount(int id, ASTNode node) {
    _tokensCount.put(id, _tokensCount.get(id) + 1);
    _tokensMap.getValue(id).recordOccurrence(node);
  }

  public void searchIdByName(String name) {
    for (Map.Entry<AbstractToken, Integer> abstractTokenIntegerEntry : this._tokensMap.getVk().entrySet()) {
      AbstractToken tk = abstractTokenIntegerEntry.getKey();
      if (tk.getName().equalsIgnoreCase(name)) {
        System.out.println(abstractTokenIntegerEntry.getValue());
      }
    }
  }

  public int getCount(int id) {
    return _tokensCount.get(id);
  }
}
