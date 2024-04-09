package cofix.core.preprocess.statementRepair.tac;

import cofix.core.match.CodeBlockMatcher;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.stmt.SwCase;
import org.eclipse.jdt.core.dom.Statement;

import java.util.*;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

public class DonorBlock implements Comparable<DonorBlock> {

    //  private String file;
//  private int line;
    private CodeBlock block;
    private double similarity;
    private static int length = 1000;


    private static Map<ASTNode, CodeBlock> donorMap = new HashMap<>();

    private static List<DonorBlock> donorList;
    private static Set<String> stringList;

    public CodeBlock getBlock() {
        return block;
    }

    public double getSimilarity() {
        return similarity;
    }

    //  public String getFile() {
//    return file;
//  }
//
//  public int getLine() {
//    return line;
//  }


    private static ASTNode getNewBlock(CodeBlock cb) {
        List<Node> list = cb.getParsedNode();
        Block newBlock = list.get(0).getOriginalAST().getAST().newBlock();
        for (Node n : list) {
            ASTNode an = n.getOriginalAST();
            newBlock.statements().add((Statement) ASTNode.copySubtree(an.getAST(), an));
        }
        return newBlock;
    }

    public static Map<ASTNode, CodeBlock> getDonorMap() {
        return donorMap;
    }

    public static ASTNode codeBlock2ASTNode(CodeBlock codeBlock) {
        if (codeBlock.getParsedNode().size() == 1) {
            if (codeBlock.getParsedNode().get(0) instanceof SwCase) {
                return codeBlock.getParsedNode().get(0).getOriginalAST().getParent();
            } else {
                return codeBlock.getParsedNode().get(0).getOriginalAST();
            }
        } else if (codeBlock.getParsedNode().size() > 1) {
            return codeBlock.getParsedNode().get(0).getOriginalAST().getParent();
        } else {
            return null;
        }
    }

//    public static Set<ASTNode> getDonorList() {
//        Set<ASTNode> set = new HashSet<>();
//        for (DonorBlock d : donorList) {
//            if (d.getBlock().getParsedNode().size() == 0) continue;
//            set.add(getNewBlock(d.getBlock()));
//        }
//        return set;
//    }

    public static void initDonorMap() {
        if (donorMap.isEmpty()) {
            for (DonorBlock d : donorList) {
                CodeBlock cd = d.getBlock();
                if (cd.getParsedNode().size() == 0) continue;
                donorMap.put(getNewBlock(cd), cd);
            }
        }
    }


    public DonorBlock(double similarity, CodeBlock block) {
        this.similarity = similarity;
        this.block = block;
    }


    @Override
    public int compareTo(DonorBlock o) {
        return Double.compare(this.similarity, o.similarity);
    }

    public static void initDonorList() {
        donorList = new ArrayList<>();
        stringList = new HashSet<>();
    }

    @Override
    public String toString() {
        return
                "similarity=" + similarity +
                        ", block=" + block;
    }

    public static void addDonorList(CodeBlock cd, CodeBlock bug) {
        int ori = stringList.size();
        if (cd == null) return;
        DonorBlock cur = new DonorBlock(CodeBlockMatcher.getSimilarity(bug, cd), cd);
        stringList.add(cd.toSrcString().toString());
        if (stringList.size() > ori) {
            if (donorList.size() < length) {
                donorList.add(cur);
            } else {
                Collections.sort(donorList);
                DonorBlock d = donorList.get(0);
                if (d.getSimilarity() < cur.getSimilarity()) {
                    donorList.remove(d);
                    donorList.add(cur);
                }
            }
        }
    }
}
