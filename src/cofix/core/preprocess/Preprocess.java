package cofix.core.preprocess;

import cofix.common.config.Constant;
import cofix.common.run.Executor;
import cofix.common.util.Subject;
import cofix.core.preprocess.statementRepair.FragmentProcessor;
import cofix.core.preprocess.statementRepair.tac.StatementProcessorWithTAC;
import cofix.core.preprocess.tokenRepair.TokensProcessor;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Preprocess {

  private final TokensProcessor _tokensProcessor = new TokensProcessor();
  private final FragmentProcessor _fragmentProcessor = new FragmentProcessor(_tokensProcessor);
  private StatementProcessorWithTAC _tacP;
  private final Subject _subject;
  private final Logger _logger = Logger.getLogger(Preprocess.class);

  public Preprocess(Subject _subject) {
    this._subject = _subject;
    _tacP=new StatementProcessorWithTAC(_subject);
    Executor.execute(new String[]{"/bin/bash", "-c",
        Constant.COMMAND_CD + _subject.getHome() + " && " + "git checkout -f HEAD "
            + _subject.getHome() + _subject.getSsrc()});
  }

  public TokensProcessor getTokensProcessor() {
    return _tokensProcessor;
  }

  public void collectCu(CompilationUnit cu, String path) {
    _tokensProcessor.collectCu(cu, path);
    if (Constant.ENABLE_STMT_FIX) {
      _fragmentProcessor.initFrequencies(cu);
      _fragmentProcessor.generateSequence(cu);
    }
  }


  public void doMining() {
    _logger.info("Mining start!");
    _tokensProcessor.doMining();
  }

  public void fix() {
    _logger.info("Fix start!");
    TestPatch testPatch = new TestPatch(_subject, _fragmentProcessor, _tokensProcessor,_tacP);
    testPatch.fix(_subject);
  }
}
