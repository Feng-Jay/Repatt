package cofix.core.preprocess;

import static cofix.core.preprocess.Patch.PatchType.sys;

import cofix.common.run.Runner;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.modification.BuggyFile;
import cofix.core.modification.EventList;
import cofix.core.modification.Modification;
import cofix.core.pattern.MatchedPattern;
import cofix.core.preprocess.tokenRepair.TokensIdentifier;
import cofix.core.select.SortUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

public class Patch implements Comparable {

    private TextEdit _edit;
    private int _occurrences = 0;
    private double _normalizedOccurrences;

    private double _similarity;
    private double _diceDistance;
    private double _priority;
    private String _afterApplied;
    private BuggyFile _buggyFile;
    private UndoEdit _undoEdit;
    private PatchType _patchType;
    private String _pattern = "";

    public String getDonor() {
        return _donor;
    }

    private String _donor;

    private Integer _blockId;

    private List<MatchedPattern> _matchedPatterns;

    private List<UndoEdit> _undoSystemEditions = new ArrayList<>();

    private EventList _eventList = null;

    private static final Logger logger = Logger.getLogger(Patch.class);

    public Patch(TextEdit edit, int occurrences, double priority, BuggyFile buggyFile, PatchType pt) {
        _edit = edit;
        _occurrences = occurrences;
        _buggyFile = buggyFile;
        _priority = priority;
        _patchType = pt;
        calculateDistance();
    }

    public Patch(TextEdit edit, int occurrences, BuggyFile buggyFile, PatchType pt, String pattern) {
        _edit = edit;
        _occurrences = occurrences;
        _buggyFile = buggyFile;
        _patchType = pt;
        _pattern = pattern;
        calculateDistance();
    }

    public Patch(TextEdit edit, BuggyFile buggyFile, PatchType pt, String donor) {
        _edit = edit;
        _buggyFile = buggyFile;
        _patchType = pt;
        _donor = donor;
        calculateDistance();
    }

    public Patch(TextEdit edit, EventList eventList, int occurrences, double priority,
                 BuggyFile buggyFile, PatchType pt) {
        _edit = edit;
        _occurrences = occurrences;
        _buggyFile = buggyFile;
        _priority = priority;
        _eventList = eventList;
        _patchType = pt;
        calculateDistance();
    }

    public Patch(TextEdit edit, EventList eventList, List<MatchedPattern> patterns, double priority,
                 BuggyFile buggyFile, PatchType pt) {
        _edit = edit;
        _matchedPatterns = patterns;
        _occurrences = patterns.stream().min(Comparator.comparing(MatchedPattern::getFrequency))
                .get().getFrequency();
        _buggyFile = buggyFile;
        _priority = priority;
        _eventList = eventList;
        _patchType = pt;
        calculateDistance();
    }

    @Override
    public int compareTo(Object o) {
        Patch obj = (Patch) o;
        if (_occurrences != obj._occurrences) {
            return obj._occurrences - _occurrences;
        } else {
            return 0;
        }
    }

    public enum PatchType {
        stmt_pattern,
        sys,
        stmt_mutate,
        token,
        combination,
    }

    public PatchType getPatchType() {
        return _patchType;
    }

    public void setPatchType(PatchType type) {
        _patchType = type;
    }

    public String printPattern(Subject sub) {
        TokensIdentifier identifier = sub.getTokensProcessor().getTokensIdentifier();
        StringBuilder sb = new StringBuilder();
        for (MatchedPattern pattern : _matchedPatterns) {
            sb.append(pattern.toString(identifier));
            if (_matchedPatterns.indexOf(pattern) != _matchedPatterns.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String getPattern() {
        return _pattern;
    }

    public void setSimilarity(double similarity) {
        _similarity = similarity;
    }

    public double getSimilarity() {
        return _similarity;
    }

    public void setBlockId(Integer id) {
        _blockId = id;
    }

    public Integer getBlockId() {
        return _blockId;
    }

    public TextEdit getEdit() {
        return _edit;
    }

    public int getOccurrences() {
        return _occurrences;
    }

    public double getDiceDistance() {
        return _diceDistance;
    }

    public EventList getModificationEvents() {
        return _eventList;
    }

    public double getPossibility() {
        return _priority * 10 + _diceDistance * 0.5 + _normalizedOccurrences * 0.5;
    }

    public void doNormalization(int maxOccurrences) {
        _normalizedOccurrences = (double) _occurrences / maxOccurrences;
    }

    public double getNormalizedOccurrence() {
        return _normalizedOccurrences;
    }

    public List<MatchedPattern> getMatchedPatterns() {
        return _matchedPatterns;
    }

    public int trySystemEdit(List<Pair<BuggyFile, Integer>> possibleLocations) throws Exception {
        logger.info("Trying SystemEdit.");
        logger.debug("Possible locations: " + possibleLocations);

        List<List<Patch>> patchList = new ArrayList<>();

        for (Pair<BuggyFile, Integer> loc : possibleLocations) {
            BuggyFile candidateBuggyFile = loc.getFirst();
            ASTNode bugNode = candidateBuggyFile.getStatement(loc.getSecond());
            if (bugNode == null) {
                continue;
            }
            List<Modification> fixs = _eventList.tryApply(bugNode, candidateBuggyFile,
                    candidateBuggyFile.getAvailableVars(loc.getSecond()));
            if (fixs.size() == 0) {
                continue;
            }
            List<Patch> candidatePatchList = new ArrayList<>();
            for (Modification fix : fixs) {
                candidatePatchList.add(new Patch(fix.buildPatch(), 1, 1, candidateBuggyFile, sys));
            }
            patchList.add(candidatePatchList);
        }

        if (patchList.size() == 0) {
            return 0;
        }

        List<Integer> numCount = new ArrayList<>();

        for (List<Patch> candidatePatchList : patchList) {
            numCount.add(candidatePatchList.size());
        }
        int count = 0;
        int successCount = 0;

        while (!(numCount.stream().reduce(Integer::sum).get() == 0)) {
            if (count > 20) {
                logger.error("Have test Over 20 combination.");
                return successCount;
            }
            logger.info("SystemEditLog: Verifying set:" + numCount);
            for (int i = 0; i < numCount.size(); i++) {
                int patchId = numCount.get(i) - 1;
                if (patchId == -1) {
                    continue;
                }
                patchList.get(i).get(patchId).apply();
            }
            ValidateStatus status = validate();
            count++;
            for (int i = 0; i < numCount.size(); i++) {
                int patchId = numCount.get(i) - 1;
                if (patchId == -1) {
                    continue;
                }
                patchList.get(i).get(patchId).undo();
            }
            if (status == ValidateStatus.SUCCESS) {
                for (int i = 0; i < numCount.size(); i++) {
                    if (numCount.get(i) == 0) {
                        continue;
                    }
                    logger.info(
                            "SystemEditLog: Applied at:" + possibleLocations.get(i).getFirst().getFilePath()
                                    + " " + possibleLocations.get(i).getSecond());
                }
                logger.info("SystemEditLog: Success.");
                successCount++;
            }
            numCount.set(numCount.size() - 1, numCount.get(numCount.size() - 1) - 1);
            for (int i = numCount.size() - 1; i >= 0; i--) {
                if (numCount.get(i) == -1) {
                    numCount.set(i - 1, numCount.get(i - 1) - 1);
                    numCount.set(i, patchList.get(i).size());
                }
            }
            if (numCount.get(0) == 0) {
                return successCount;
            }
        }
        return successCount;
    }

    public String getFixedString() {
        return _afterApplied;
    }

    public void calculateDistance() {
        Document buggyDoc = _buggyFile.getDocument();
        try {
            IRegion beginLine = buggyDoc.getLineInformationOfOffset(_edit.getRegion().getOffset());
            IRegion endLine = buggyDoc.getLineInformationOfOffset(
                    _edit.getRegion().getOffset() + _edit.getRegion().getLength() + 1);
            String source = buggyDoc.get()
                    .substring(beginLine.getOffset(), endLine.getOffset() + endLine.getLength());
            UndoEdit undo = _edit.apply(buggyDoc);
            beginLine = buggyDoc.getLineInformationOfOffset(_edit.getRegion().getOffset());
            endLine = buggyDoc.getLineInformationOfOffset(
                    _edit.getRegion().getOffset() + _edit.getRegion().getLength() + 1);
            _afterApplied =
                    buggyDoc
                            .get()
                            .substring(beginLine.getOffset(), endLine.getOffset() + endLine.getLength()).trim();
            _edit = undo.apply(buggyDoc);
            _diceDistance = SortUtil.normalizedDistance(source, _afterApplied);
        } catch (BadLocationException e) {
            System.out.println(e);
        }
    }

    public BuggyFile getBuggyFile() {
        return _buggyFile;
    }

    public void apply() throws BadLocationException {
        _undoEdit = _edit.apply(_buggyFile.getDocument());
        JavaFile.writeStringToFile(_buggyFile.getFilePath(), _buggyFile.getDocument().get());
    }

    public void undo() throws BadLocationException {
        _edit = _undoEdit.apply(_buggyFile.getDocument());
        JavaFile.writeStringToFile(_buggyFile.getFilePath(), _buggyFile.getDocument().get());
    }

    public ValidateStatus validate() throws BadLocationException {
        apply();
        if (!Runner.compileSubject(_buggyFile.getSubject())) {
            //			System.err.println("Build failed !");
            undo();
            return ValidateStatus.COMPILE_FAILED;
        }

        // validate patch using failed test cases
        for (String testcase : _buggyFile.getSubject().getAbstractFaultlocalization()
                .getFailedTestCases()) {
            String[] testinfo = testcase.split("::");
            if (!Runner.testSingleTest(_buggyFile.getSubject(), testinfo[0], testinfo[1])) {
                undo();
                return ValidateStatus.TEST_FAILED;
            }
        }

        if (!Runner.runTestSuite(_buggyFile.getSubject())) {
            undo();
            return ValidateStatus.TEST_FAILED;
        }

        undo();
        return ValidateStatus.SUCCESS;
    }

    public enum ValidateStatus {
        COMPILE_FAILED,
        TEST_FAILED,
        SUCCESS
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Patch)) {
            return false;
        }
        return _afterApplied.equals(((Patch) obj).getFixedString());
    }

    public int hashCode() {
        return _afterApplied.hashCode();
    }

}
