package cofix.core.select;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class SortUtil {

  public static List<String> split(String input) {
    String temp = input.replaceAll("[A-Z]", " $0");
    return Arrays.stream(temp.split("[.\\s+\"()]"))
        .filter(p -> !p.equals("")).collect(Collectors.toList());
  }

  /*
    public static double diceDistance(String source,String target){
      List<String> sourceList = split(source);
      List<String> targetList= split(target);
      int sourceSize = sourceList.size();
      int targetSize = targetList.size();
      int[][] lcs = new int[sourceSize + 1][targetSize + 1];
      for (int i = 0; i <= sourceSize; i++) {
        for (int j = 0; j <= targetSize; j++) {
          lcs[i][j] = 0;
        }
      }
  
      for (int i = 1; i <= sourceSize; i++) {
        for (int j = 1; j <= targetSize; j++) {
          if (sourceList.get(i - 1).equals(targetList.get(j - 1))) {
            lcs[i][j] = lcs[i - 1][j - 1] + 1;
          } else {
            lcs[i][j] = Math.max(lcs[i - 1][j], lcs[i][j - 1]);
          }
        }
      }
  
      return 2*(double)lcs[sourceSize][targetSize]/(sourceSize+targetSize);
    }
  */
/*
  public static double diceDistance(String s, String t)
  {
    // Verifying the input:
    if (s == null || t == null)
      return 0;
    // Quick check to catch identical objects:
    if (s == t)
      return 1;
    // avoid exception for single character searches
    if (s.length() < 2 || t.length() < 2)
      return 0;

    // Create the bigrams for string s:
    final int n = s.length()-1;
    final int[] sPairs = new int[n];
    for (int i = 0; i <= n; i++)
      if (i == 0)
        sPairs[i] = s.charAt(i) << 16;
      else if (i == n)
        sPairs[i-1] |= s.charAt(i);
      else
        sPairs[i] = (sPairs[i-1] |= s.charAt(i)) << 16;

    // Create the bigrams for string t:
    final int m = t.length()-1;
    final int[] tPairs = new int[m];
    for (int i = 0; i <= m; i++)
      if (i == 0)
        tPairs[i] = t.charAt(i) << 16;
      else if (i == m)
        tPairs[i-1] |= t.charAt(i);
      else
        tPairs[i] = (tPairs[i-1] |= t.charAt(i)) << 16;

    // Sort the bigram lists:
    Arrays.sort(sPairs);
    Arrays.sort(tPairs);

    // Count the matches:
    int matches = 0, i = 0, j = 0;
    while (i < n && j < m)
    {
      if (sPairs[i] == tPairs[j])
      {
        matches += 2;
        i++;
        j++;
      }
      else if (sPairs[i] < tPairs[j])
        i++;
      else
        j++;
    }
    return (double)matches/(n+m);
  }
*/
  public static double normalizedDistance(String s, String t) {
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    int distance = levenshteinDistance.apply(s, t);
    return 1 - (double) distance / Math.max(s.length(), t.length());
  }

}
