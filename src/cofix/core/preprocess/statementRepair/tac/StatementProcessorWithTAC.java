package cofix.core.preprocess.statementRepair.tac;

import cofix.common.config.Constant;
import cofix.common.run.Executor;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.modification.BuggyFile;
import cofix.core.modification.Modification;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.search.BuggyCode;
import cofix.core.preprocess.Patch;
import cofix.core.preprocess.Patch.PatchType;
import cofix.core.preprocess.Patch.ValidateStatus;
import cofix.core.preprocess.Tree;
import cofix.core.preprocess.statementRepair.FragmentBugFinder;
import cofix.core.preprocess.statementRepair.FragmentProcessor.InsertType;

import java.util.*;
import java.util.Map.Entry;

import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.TextEdit;

public class StatementProcessorWithTAC {

    private Subject subject;
    private BuggyFile buggyFile;
    private final Set<Patch> oriPatches = new HashSet<>();
    private Pair<String, Integer> loc;
    private FragmentBugFinder bugFinder;
    private Logger logger = Logger.getLogger(StatementProcessorWithTAC.class);
    private int bigBlock = 0;


    public StatementProcessorWithTAC(Subject subject) {
        this.subject = subject;
    }


    public void doFix(Pair<String, Integer> loc) {
        oriPatches.clear();
        //init fix
        this.loc = loc;
        buggyFile = new BuggyFile(subject, loc);
        bugFinder = new FragmentBugFinder(loc.getSecond(), buggyFile.getCompilationUnit());
        CodeBlock bugCodeBlock = BuggyCode.getBuggyCodeBlock(buggyFile.getFilePath(), loc.getSecond());
        ASTNode bugNode =
                bugFinder.getBuggyNode(buggyFile.getFilePath()) == null ? bugFinder.getBugStatment()
                        : bugFinder.getBuggyNode(buggyFile.getFilePath());
        Node2Tokens p = new Node2Tokens();
        p.process(bugNode);
        Set<String> tokens = p.getTokens();
        Set<ASTNode> blocks = collectSimilarCode(bugCodeBlock, tokens);
        logger.info(
                subject.getName() + subject.getId() + " collect " + blocks.size() + " donor code blocks");
        NodeCheckTAC bugProcess = new NodeCheckTAC(bugNode);
        bugProcess.processTAC();
        List<TAC> bugList = bugProcess.TACList;
        //Set<Map<Integer, List<TAC>>> allFix = new HashSet<>();
        logger.info("start to process donors");


        for (ASTNode donor : ProgressBar.wrap(blocks, "Process Donors")) {
            if (donor == null) {
                continue;
            }
            StringBuffer sb = new StringBuffer();
            sb.append(donor.toString());
            sb.append("===============================\n");
            JavaFile.writeStringToFile("tac_logs/" + subject.getName() + subject.getId() + "/blocks.txt", sb.toString(), true);

            NodeCheckTAC donorProcess = new NodeCheckTAC(donor);
            donorProcess.processTAC();
            List<TAC> donorList = donorProcess.TACList;

//            ASTNode completeBlock = DonorBlock.codeBlock2ASTNode(DonorBlock.getDonorMap().get(donor));
//            NodeCheckTAC completeProcess = new NodeCheckTAC(completeBlock);
//            completeProcess.processTAC();
//            List<TAC> completeList = completeProcess.TACList;
//            int infix = getInfixIndex(completeList, donorList);

            if (donorList.size() >= 80) {
                bigBlock++;
            }
//            logger.info("===============================\n");
//            logger.info(donor.toString());
            Set<Map<Integer, List<TAC>>> donorFix = getFixMap(bugList, donorList);
            // logger.info("generated fix map");
            if (donorFix == null) {
                continue;
            }

            for (Map<Integer, List<TAC>> fix : donorFix) {
                oriPatches.addAll(tacFix(fix, bugList, donor.toString()));
            }
        }

        logger.info(
                subject.getName() + subject.getId() + " generate " + oriPatches.size() + " TAC patches");
    }

    private boolean isCommon(String str) {
        if (str.equals("equals") || str.equals("toString") || str.equals("append")) {
            return true;
        } else {
            return false;
        }
    }

    private int getInfixIndex(List<TAC> completeList, List<TAC> donorList) {
        /* input donor list L1 and complete list L2, L1(except the last TAC) is a part of L2
         * getInfixIndex() will return a value that a same node's index in L1 + value equals index in L2
         */
        for (int i = 0; i < completeList.size(); i++) {
            TAC cur = completeList.get(i);
            if (cur.getLabel().equals(donorList.get(0).getLabel())) {
                boolean flag = true;
                int k = 1;
                for (int j = i + 1; k < donorList.size() - 1; j++) {
                    if (!completeList.get(j).getLabel().equals(donorList.get(k).getLabel())) {
                        flag = false;
                        break;
                    }
                    k++;
                }
                if (flag) {
                    return i;
                }
            }
        }
        return -1;//error
    }

    private Set<ASTNode> collectSimilarCode(CodeBlock bugBlock, Set<String> tokens) {
        logger.info(subject.getName() + subject.getId() + " start collect donor code");
        DonorBlock.initDonorList();

        for (String token : tokens) {
            List<String> message = new ArrayList<>();
            if (isCommon(token)) {
                continue;
            }

            String cmd =
                    Constant.COMMAND_CD + subject.getHome() + subject.getSsrc() +
                            "&& grep -n -r -v --include \"*.java\" -E \"^\\s*/\\*|^\\s*//|^\\s*\\*\"  | grep \"\\b"
                            +
                            token + "\\b\"";
            message.addAll(Executor.execute(new String[]{"/bin/bash", "-c", cmd}));
            logger.info("There are " + message.size() + " messages related to \"" + token + "\" ");
            int cnt = 0;
            int threshold = 2000;
            for (String m : message) {
                if (cnt > threshold) {
                    logger.info("more than " + threshold + " code blocks contain \"" + token + "\" ");
                    break;
                }
                cnt++;
                if (m.startsWith("./")) {
                    m = m.substring(2);
                }
                String file =
                        subject.getHome() + subject.getSsrc() + "/" + m.split(
                                ":")[0];
                Integer line = Integer.valueOf(m.split(":")[1]);
                if (file.equals(loc.getFirst()) && line.equals(loc.getSecond())) {
                    continue;
                }
                CodeBlock cd = BuggyCode.getBuggyCodeBlock(file, line);
                DonorBlock.addDonorList(cd, bugBlock);
            }
            logger.info("found  code blocks containing \"" + token + "\" ");
            logger.info("=======================");
        }
        DonorBlock.initDonorMap();
        return DonorBlock.getDonorMap().keySet();
    }


    private Set<Map<Integer, List<TAC>>> getFixMap(List<TAC> bug, List<TAC> donor) {
        Set<Map<Integer, List<TAC>>> allFix = new HashSet<>();
        List<Integer> donorList = TAC.tacLabel2Int(donor);
        List<Integer> bugList = TAC.tacLabel2Int(bug);
        if (Collections.disjoint(donorList, bugList)) {
            return null;
        }
        Set<List<Integer>> allMatch = Tree.findLCS4Tac2(TAC.tac2Int(donor), TAC.tac2Int(bug));

        List<Set<Integer>> group = findTACGroup(bug);
        for (List<Integer> match : allMatch) {
            //match 对应情况
            Map<Integer, List<TAC>> map = new HashMap<>();
            //meaningless
            if (match.stream().allMatch(element -> element == -1)) {
                return null;
            }
            //开区间
            int front = -1;
            int back = donor.size() - 1;

            for (int i = 0; i < match.size(); i++) {
                if (match.get(i) == -1) {
                    //找到当前index所在的group
                    Set<Integer> current = new HashSet<>();
                    for (Set<Integer> g : group) {
                        if (g.contains(i)) {
                            current = g;
                            break;
                        }
                    }
                    //找front:donor List 中的index
                    for (int j = 0; j < i; j++) {
                        if (match.get(j) != -1 && !current.contains(j)) {
                            front = match.get(j);//  the last one != -1 //判断是否同族
                        }
                    }
                    //找back
                    for (int j = i + 1; j < match.size(); j++) {
                        if (match.get(j) != -1 && !current.contains(j)) {
                            back = match.get(j);// the first one != -1
                            break;
                        }
                    }
                    List<TAC> fix = new ArrayList<>();
                    for (int j = front + 1; j < back; j++) {
                        fix.add(donor.get(j));
                    }
                    map.put(i, fix);//i :index in bug
                }
            }
            allFix.add(map);
        }
        return allFix;
    }

    private List<Set<Integer>> findTACGroup(List<TAC> tacs) {
        Set<Integer> indexs = new HashSet<>();
        List<Set<Integer>> group = new ArrayList<>();
        for (int i = 0; i < tacs.size() - 1; i++) {
            ASTNode current = tacs.get(i).getTACNode();
            ASTNode next = tacs.get(i + 1).getTACNode();
            if (current.getParent().equals(next)) {
                indexs.add(i);
                indexs.add(i + 1);
            } else {
                indexs.add(i);
                group.add(indexs);
                indexs = new HashSet<>();
            }
        }
        group.add(indexs);
        return group;
    }

    private List<Patch> tacFix(Map<Integer, List<TAC>> fix, List<TAC> bugList, String nodeStr) {
        List<Patch> result = new LinkedList<>();
        List<Modification> modiList = new ArrayList<>();

        for (Entry<Integer, List<TAC>> entry : fix.entrySet()) {
            //a node in bug
            ASTNode ori = bugList.get(entry.getKey()).getTACNode();
            for (TAC tac : entry.getValue()) {
                // different replacements for the node
                Modification replaceModi = new Modification(buggyFile);
                ASTNode target = tac.getTACNode();
                if (isValidReplace(ori, target)) {
                    replaceModi.replace(ori, target);
                    modiList.add(replaceModi);
                }
                Modification insertModi = new Modification(buggyFile);
                while (!(ori instanceof Statement)) {
                    ori = ori.getParent();
                }
                insertModi.insertBefore(ori, target, getInsertType(target));
                modiList.add(insertModi);
            }
        }
        for (Modification m : modiList) {
            TextEdit edit = m.buildPatch();
            Patch patch = new Patch(edit, buggyFile, PatchType.stmt_pattern,
                    nodeStr);
            if (patch.getFixedString() != null) {
                result.add(patch);
            }
        }

        //showPatch(result);
        return result;
    }

    public void showPatch(List<Patch> list) {
        for (Patch p : list) {
            System.out.println(p.getFixedString());
        }
    }

    public InsertType getInsertType(ASTNode target) {
        if ((target instanceof Expression)) {
            if (target.getParent() instanceof IfStatement) {
                return InsertType.insertCondition;
            } else if (target.getParent() instanceof Expression && target.getParent()
                    .getParent() instanceof IfStatement) {
                return InsertType.insertCondition;
            }
        }
        return InsertType.insert;
    }

    public boolean isValidReplace(ASTNode ori, ASTNode target) {
        //ori和target返回值类型相同
        if (ori instanceof MethodInvocation && target instanceof MethodInvocation) {
            IMethodBinding oriBinding = ((MethodInvocation) ori).resolveMethodBinding();
            IMethodBinding targetBinding = ((MethodInvocation) target).resolveMethodBinding();
            if (oriBinding != null && targetBinding != null) {
                return ((MethodInvocation) ori).resolveMethodBinding().getReturnType()
                        .equals(((MethodInvocation) target).resolveMethodBinding().getReturnType());
            }
        }

        //level相同：expression<->expression, statement<->statement
        if (ori instanceof Statement) {
            return target instanceof Statement;
        } else {
            return !(target instanceof Statement);
        }

        //valid var

    }

    public int validatePatches(Logger _logger) {
        int compileCnt = 0;
        int failCompile = 0;
        int successCnt = 0;
        long startTime = System.currentTimeMillis();

        _logger.info("Current Loc: " + loc.getFirst() + " " + loc.getSecond() + "\n");
        Executor.execute(new String[]{"/bin/bash", "-c",
                Constant.COMMAND_CD + subject.getHome() + " && " + "git checkout -f HEAD "
                        + subject.getHome() + subject.getSsrc()});

        int testCount = 0;
        FOUND:
        for (Patch patch : oriPatches) {
            if (testCount++ >= Constant.ALL_STMT_MAX_PATCH_ONE_LOCATION) {
                _logger.info("TAC patch test finished");
                break;
            }

            if (successCnt >= Constant.STMT_MAX_PATCH_ONE_LOCATION) {
                _logger.info("patches are already successful in this location");
                break;
            }

            if (compileCnt >= Constant.MAX_GENERATED_PATCH) {
                _logger.info("Too much patches can't be compiled!");
                break;
            }

            try {
                _logger.info("No. " + testCount + " TAC Testing:" + patch.getFixedString());

                ValidateStatus status = patch.validate();
                long timeForPatch = System.currentTimeMillis() - startTime;
                switch (status) {
                    case SUCCESS:
                        ++compileCnt;
                        ++successCnt;
                        _logger.info("=======================");
                        _logger.info("Time: " + timeForPatch / 1000 + "s");
                        _logger.info("No." + (successCnt) + "/" + testCount + " TEST SUCCESSFUL with TAC");
                        _logger.info(patch.getFixedString());
                        _logger.info(
                                "Occurrence: " + patch.getOccurrences() + " DiceDistance: "
                                        + patch.getDiceDistance()
                                        + " Score: " + patch.getPossibility());
                        if (patch.getPatchType().equals(PatchType.stmt_pattern)) {
                            _logger.info("Donor: " + patch.getDonor());
                        }
                        _logger.info("=======================");
                        break;
                    case TEST_FAILED:
                        _logger.info("TEST FAILED");
                        compileCnt++;
                        break;
                    case COMPILE_FAILED:
                        failCompile++;
                        _logger.info("=======================");
                        System.out.println("COMPILE FAILED");
                        if (patch.getPatchType().equals(PatchType.stmt_pattern)) {
                            _logger.info("Donor: " + patch.getDonor());
                        }
                        _logger.info("Patch:" + patch.getFixedString());
                        _logger.info("=======================");
                        break;
                }
            } catch (BadLocationException e) {
            } finally {
                Executor.execute(new String[]{"/bin/bash", "-c",
                        Constant.COMMAND_CD + subject.getHome() + " && " + "git checkout -f HEAD "
                                + subject.getHome() + subject.getSsrc()});
                patch.getBuggyFile().reloadFile();
            }
        }
        _logger.info("Num of Test Failed Patch: " + (compileCnt - successCnt));
        _logger.info("Num of Test Pass Patch：" + successCnt);
        _logger.info("Num of Compile Failed Patch：" + failCompile);

        return successCnt;
    }

    private List<Pair<Integer, ASTNode>> testFixNode(String s, Set<Map<Integer, List<TAC>>> allFix) {
        //输入string 查找有无replace为对应string的node以及具体替换方式
        List<Pair<Integer, ASTNode>> result = new ArrayList<>();
        for (Map<Integer, List<TAC>> fix : allFix) {
            for (Entry<Integer, List<TAC>> entry : fix.entrySet()) {
                for (TAC t : entry.getValue()) {
                    if (t.getTACNode().toString().equals(s)) {
                        result.add(new Pair<>(entry.getKey(), t.getTACNode()));
                    }
                }
            }
        }
        return result;
    }

}

