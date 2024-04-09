
package cofix.core.preprocess;

import cofix.common.config.Constant;
import cofix.common.run.Executor;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.Divider;
import cofix.core.preprocess.Patch.ValidateStatus;
import cofix.core.preprocess.statementRepair.FragmentProcessor;
import cofix.core.preprocess.statementRepair.Group;
import cofix.core.preprocess.statementRepair.tac.StatementProcessorWithTAC;
import cofix.core.preprocess.tokenRepair.TokensProcessor;
import cofix.main.Timer;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.BadLocationException;

public class TestPatch {

    private static int numOfPass = 0;
    private final Subject _subject;
    private static final StringBuffer sb = new StringBuffer();
    private final FragmentProcessor _fgp;
    private final TokensProcessor _tkp;
    private final StatementProcessorWithTAC _tacP;
    private List<Pair<String, Integer>> locations;
    private final Logger _logger = Logger.getLogger(TestPatch.class);

    public TestPatch(Subject subject, FragmentProcessor fgp, TokensProcessor tkp,
                     StatementProcessorWithTAC tacP) {
        _subject = subject;
        _fgp = fgp;
        _tkp = tkp;
        _tacP = tacP;
    }

    public static int getNumOfPass() {
        return numOfPass;
    }

    public void fix(Subject subject) {
        Group.buildMap(_subject);
        locations = _subject.getAbstractFaultlocalization().getLocations(200);
        Divider systemDivider = new Divider();
        Timer timer = new Timer(5, 0);
        timer.start();
        for (Pair<String, Integer> loc : locations) {
            if (timer.timeout()) {
                _logger.info("Timeout for the project");
                System.exit(0);
            }
            if (numOfPass >= 6) {
                _logger.info("Passed 6 times");
                System.exit(0);
            }
            _logger.info("Trying location: " + loc.getFirst() + ":" + loc.getSecond());
            if (Constant.ENABLE_STMT_FIX) {
                _logger.info("Start to make fragments repair");
                _fgp.doFix(subject, loc);
            }
            if (Constant.ENABLE_TAC_FIX) {
                _logger.info("Start to make TAC repair");
                _tacP.doFix(loc);
            }
            if (Constant.ENABLE_TOKEN_FIX) {
                _logger.info("Start to make tokens repair");
                _tkp.doFix(subject, loc, systemDivider);
            }

            if (Constant.ENABLE_TEST) {
                testAll(loc);
            }
        }
    }

    public void testAll(Pair<String, Integer> loc) {
        if (Constant.ENABLE_TOKEN_FIX) {
            numOfPass += TokensProcessor.validatePatches(_subject, _tkp.getBuggyFile(),
                    _tkp.getSortedPatches(), loc, true, _tkp.getSimilarLocations());
        }
        if (Constant.ENABLE_STMT_FIX) {
            numOfPass += testFragment(_fgp.getSortedPatches(), loc, _subject);
        }
        if (Constant.ENABLE_TAC_FIX) {
            numOfPass += _tacP.validatePatches(_logger);
        }
    }


    public int testFragment(List<Patch> sortedPatches, Pair<String, Integer> loc, Subject subject) {
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
        for (Patch patch : sortedPatches) {
            if (testCount++ >= Constant.ALL_STMT_MAX_PATCH_ONE_LOCATION) {
                _logger.info("Stmt level patch test finished");
                break;
            }
            if (successCnt >= Constant.STMT_MAX_PATCH_ONE_LOCATION) {
                _logger.info("patches are already successful in this location");
                break;
            }
/*
    if (compileCnt >= Constant.STMT_TEST_NUM) {
      _logger.info("patches are already failed in this location");
      break;
    }
*/
            try {
                _logger.info("No. " + testCount + " Testing:" + patch.getFixedString());

                ValidateStatus status = patch.validate();
                long timeForPatch = System.currentTimeMillis() - startTime;
                startTime = System.currentTimeMillis();
                switch (status) {
                    case SUCCESS:
                        ++compileCnt;
                        ++successCnt;
                        _logger.info("=======================");
                        _logger.info("Time: " + timeForPatch / 1000 + "s");
                        _logger.info("No." + (successCnt) + "/" + testCount + " TEST SUCCESSFUL with "
                                + patch.getPatchType().toString());
                        _logger.info(patch.getFixedString());
                        _logger.info(patch.getPattern() == null ? "Pattern not available" : patch.getPattern());
                        _logger.info(
                                "Occurrence: " + patch.getOccurrences() + " DiceDistance: "
                                        + patch.getDiceDistance()
                                        + " Score: " + patch.getPossibility());
                        _logger.info("=======================");
                        break;
                    case TEST_FAILED:
                        _logger.info("TEST FAILED");
                        compileCnt++;
                        break;
                    case COMPILE_FAILED:
                        failCompile++;
                        System.out.println("COMPILE FAILED");
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
}

