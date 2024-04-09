package cofix.common.util;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListTool {
    public static List<List<Integer>> splitStream(List<Integer> input, Integer separator) {
        int sz = input.size();
        int[] indexes =
                IntStream.rangeClosed(-1, sz)
                        .filter(i -> i == -1 || i == sz || input.get(i).equals(separator))
                        .toArray();

        return IntStream.range(0, indexes.length - 1)
                .mapToObj(i -> input.subList(indexes[i] + 1, indexes[i + 1]))
                .collect(toList());
    }

    public static List<Integer> intArrayToList(int[] array) {
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }
}
