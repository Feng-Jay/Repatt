package cofix.core.preprocess;

import cofix.common.config.Constant;
import cofix.common.util.ListTool;
import cofix.common.util.Pair;
import cofix.core.pattern.MatchedPattern;
import cofix.core.preprocess.statementRepair.tac.TAC;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class Tree {

    private final Map<Integer, TreeNode> _roots;
    private List<MatchedPattern> patterns = new ArrayList<>();
    private final double SIMILARITY = 0.15;
    private static Logger logger = Logger.getLogger(Tree.class);

    public Tree() {
        _roots = new HashMap<>();
    }

    public boolean isEmpty() {
        return _roots.isEmpty();
    }

    public void build(Queue<Integer> sequence, Tree mirror) {
        if (sequence.size() >= 2) {
            Integer top = sequence.poll();
            TreeNode node = _roots.get(top);
            if (node == null) {
                node = new TreeNode();
                _roots.put(top, node);
            }
            TreeNode mirrorNode = mirror._roots.get(top);
            boolean exist = true;
            if (mirrorNode == null) {
                exist = false;
                mirrorNode = new TreeNode();
                mirror._roots.put(top, mirrorNode);
            }
            node.find(sequence, exist, mirrorNode);
        }
    }

    public List<MatchedPattern> do_dfs(List<Integer> keyId) {
        patterns = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
        dfs(_roots, stack, keyId, 0);
        return patterns;
    }

    public void dfs(Map<Integer, TreeNode> node, Stack<Integer> stack, List<Integer> keyId, int support) {
        if (node.isEmpty()) {
            List<Integer> onePattern = new ArrayList<>(stack);
            Pair<Double, Set<List<Integer>>> pair = calculateSimilarity(keyId, onePattern);
            if (pair.getFirst() >= SIMILARITY) {
                for (List<Integer> p : pair.getSecond()) {
                    patterns.add(new MatchedPattern(onePattern, p.stream().mapToInt(i -> i).toArray(), support));
                }
                return;
            }
        }
        boolean hasRequiredSon = false;
        for (Entry<Integer, TreeNode> entry : node.entrySet()) {
            if (entry.getValue().getFrequency() >= Constant.TOKEN_MIN_SUPPORT) {
                hasRequiredSon = true;
                stack.push(entry.getKey());
                dfs(entry.getValue().getEdges(), stack, keyId, entry.getValue().getFrequency());
                stack.pop();
            }
        }
        if (!hasRequiredSon) {
            List<Integer> onePattern = new ArrayList<>(stack);
            Pair<Double, Set<List<Integer>>> pair = calculateSimilarity(keyId, onePattern);
            if (pair.getFirst() >= SIMILARITY) {
                for (List<Integer> p : pair.getSecond()) {
                    patterns.add(new MatchedPattern(onePattern, p.stream().mapToInt(i -> i).toArray(), support));
                }
            }
        }
    }

    public static Pair<Double, Set<List<Integer>>> calculateSimilarity(List<Integer> keyList, List<Integer> patterns) {

        int keySize = keyList.size();
        int patternSize = patterns.size();

        int[][] lcs = new int[keySize + 1][patternSize + 1];
        for (int i = 0; i <= keySize; i++) {
            for (int j = 0; j <= patternSize; j++) {
                lcs[i][j] = 0;
            }
        }

        for (int i = 1; i <= keySize; i++) {
            for (int j = 1; j <= patternSize; j++) {
                if (keyList.get(i - 1).equals(patterns.get(j - 1))) {
                    lcs[i][j] = lcs[i - 1][j - 1] + 1;
                } else {
                    lcs[i][j] = Math.max(lcs[i - 1][j], lcs[i][j - 1]);
                }
            }
        }
        List<Integer> match = new ArrayList<>(patternSize);

        for (int i = 0; i < patternSize; i++) {
            match.add(-1);
        }

        return new Pair<>((((double) lcs[keySize][patternSize]) / patternSize), getMatch(keySize, patternSize, lcs, match));
    }

    public static Set<List<Integer>> findLCS4Tac2(List<Integer> keyList, List<Integer> patterns) {
        int m = patterns.size();
        int n = keyList.size();
        int[][] dp = new int[m + 1][n + 1];
        char[][] direct = new char[m + 1][n + 1];
        int[] res = new int[m];
        Arrays.fill(res, -1);
        Set<List<Integer>> result = new HashSet<>();
        if (m == 0 || n == 0) {
            result.add(ListTool.intArrayToList(res));
            return result;
        }

        for (int i = 0; i <= m; i++) {
            dp[i][0] = 0;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = 0;
        }
        for (int i = 1; i <= m; i++) {
            String donor = TAC.getTACMap().getValue(patterns.get(i - 1)).getLabel();
            for (int j = 1; j <= n; j++) {
                String key = TAC.getTACMap().getValue(keyList.get(j - 1)).getLabel();
                if (key.equals(donor)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    direct[i][j] = 'm';     // match
                } else {
                    if (dp[i - 1][j] >= dp[i][j - 1]) {
                        dp[i][j] = dp[i - 1][j];
                        direct[i][j] = 's';     // str+1
                    } else {
                        dp[i][j] = dp[i][j - 1];
                        direct[i][j] = 'r';     // ref+1
                    }
                }
            }
        }
        for (int i = m, j = n; i > 0 && j > 0; ) {
            if (direct[i][j] == 'm') {
                res[i - 1] = j - 1;
                i--;
                j--;
            } else if (direct[i][j] == 's') {
                i--;
            } else if (direct[i][j] == 'r') {
                j--;
            }
        }
        result.add(ListTool.intArrayToList(res));
        return result;
    }

    public static Set<List<Integer>> findLCS4Tac(List<Integer> keyList, List<Integer> patterns) {

        int keySize = keyList.size();
        int patternSize = patterns.size();

        int[][] lcs = new int[keySize + 1][patternSize + 1];
        for (int i = 0; i <= keySize; i++) {
            for (int j = 0; j <= patternSize; j++) {
                lcs[i][j] = 0;
            }
        }

        for (int i = 1; i <= keySize; i++) {
            String key = TAC.getTACMap().getValue(keyList.get(i - 1)).getLabel();
            for (int j = 1; j <= patternSize; j++) {
                String donor = TAC.getTACMap().getValue(patterns.get(j - 1)).getLabel();
                if (key.equals(donor)) {
                    lcs[i][j] = lcs[i - 1][j - 1] + 1;
                } else {
                    lcs[i][j] = Math.max(lcs[i - 1][j], lcs[i][j - 1]);
                }
            }
        }

        //  logger.info("find lcs");

        List<Integer> match = new ArrayList<>(patternSize);

        for (int i = 0; i < patternSize; i++) {
            match.add(-1);
        }

        return getMatch(keySize, patternSize, lcs, match);
    }


    private static Set<List<Integer>> getMatch(int i, int j, int[][] lcs, List<Integer> oriMatch) {
        List<Integer> match = new ArrayList<>(oriMatch);
        Set<List<Integer>> ans = new HashSet<>();
        if (lcs[i][j] == 0) {
            ans.add(match);
            return ans;
        }
        if (lcs[i][j] == lcs[i - 1][j] || lcs[i][j] == lcs[i][j - 1]) {
            if (lcs[i][j] == lcs[i - 1][j]) {
                ans.addAll(getMatch(i - 1, j, lcs, match));
            }
            if (lcs[i][j] == lcs[i][j - 1]) {
                ans.addAll(getMatch(i, j - 1, lcs, match));
            }
        } else {
            match.set(j - 1, i - 1);
            ans.addAll(getMatch(i - 1, j - 1, lcs, match));
        }
        return ans;
    }
}
