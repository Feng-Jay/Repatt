package cofix.core.preprocess;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Builder {

  private static final int MAX_SKIP = 8;

  public static void buildSeqs(List<Queue<Integer>> result, List<Integer> tokens, int length) {
    // build tree
    if (length < 5) {
      seq(tokens, new LinkedList<>(), result, 0, 0, length);
      buildSeqs(result, tokens, length + 1);
    }
  }

  private static void seq(
      List<Integer> tokens,
      Queue<Integer> queue,
      List<Queue<Integer>> result,
      int start,
      int skip,
      int length) {
    if (length == 0) {
      result.add(queue);
    } else {
      if (start < tokens.size()) {
        // take current token
        Queue<Integer> copy = new LinkedList<>(queue);
        copy.add(tokens.get(start));
        seq(tokens, copy, result, start + 1, 0, length - 1);
        if (skip < MAX_SKIP) {
          // skip current token
          seq(tokens, queue, result, start + 1, skip + 1, length);
        }
      }
    }
  }
}
