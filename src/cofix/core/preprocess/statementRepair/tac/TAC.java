package cofix.core.preprocess.statementRepair.tac;

import cofix.common.util.DuoMap;

import java.util.*;

import org.eclipse.jdt.core.dom.ASTNode;

public class TAC {

    private String label; // T= T+ guess
    private List<ASTNode> nodes;
    private static DuoMap<Integer, TAC> TACMap = new DuoMap<>();
    private static Integer num = 0;

    private static DuoMap<Integer, String> TacStringMap = new DuoMap<>();
    private static Integer stringNum = 0;


    public void initPool() {
        TACMap = new DuoMap<>();
        num = 0;
    }

    public ASTNode getTACNode() {
        return nodes.get(0);
    }

    public TAC(String label, List<ASTNode> nodes) {
        this.label = label;
        this.nodes = nodes;
        if (!TACMap.containsValue(this)) {
            TACMap.put(num++, this);
        }
        if (!TacStringMap.containsValue(label)) {
            TacStringMap.put(stringNum++, label);
        }
    }

    public static DuoMap<Integer, TAC> getTACMap() {
        return TACMap;
    }

    public static DuoMap<Integer, String> getTacStringMap() {
        return TacStringMap;
    }

    @Override
    public String toString() {
        return
                "label='" + label + '\t' +
                        " nodes=" + nodes +
                        '}';
    }

    public String getLabel() {
        return label;
    }

    public List<ASTNode> getNodes() {
        return nodes;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TAC tac = (TAC) o;
        return Objects.equals(label, tac.label) && Objects.equals(nodes, tac.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, nodes);
    }

    public static List<Integer> tac2Int(List<TAC> tacs) {
        List<Integer> ans = new LinkedList<>();
        for (TAC tac : tacs) {
            ans.add(getTACMap().getKey(tac));
        }
        return ans;
    }

    public static List<Integer> tacLabel2Int(List<TAC> tacs) {
        List<Integer> ans = new LinkedList<>();
        for (TAC tac : tacs) {
            ans.add(getTacStringMap().getKey(tac.getLabel()));
        }
        return ans;
    }
}
