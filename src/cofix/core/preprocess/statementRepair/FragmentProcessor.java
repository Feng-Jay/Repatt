package cofix.core.preprocess.statementRepair;

import static cofix.core.preprocess.statementRepair.Enum.MatchType.middle;
import static cofix.core.preprocess.statementRepair.Enum.MatchType.previous;
import static cofix.core.preprocess.statementRepair.FragmentProcessor.InsertType.insert;
import static cofix.core.preprocess.statementRepair.FragmentProcessor.InsertType.insertCondition;
import static cofix.core.preprocess.statementRepair.FragmentProcessor.InsertType.invalid;

import cofix.common.config.Constant;
import cofix.common.run.Executor;
import cofix.common.util.DuoMap;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.modification.BuggyFile;
import cofix.core.modification.Modification;
import cofix.core.parser.search.CodeSearch;
import cofix.core.pattern.MatchedPattern;
import cofix.core.preprocess.Builder;
import cofix.core.preprocess.statementRepair.Enum.MatchType;
import cofix.core.preprocess.Identifier;
import cofix.core.preprocess.Patch;
import cofix.core.preprocess.Patch.PatchType;
import cofix.core.preprocess.Tree;
import cofix.core.preprocess.pattern.Fragment;
import cofix.core.preprocess.tokenRepair.TokensProcessor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.text.edits.TextEdit;

public class FragmentProcessor {

  private final DuoMap<Integer, Fragment> _frequentIdMap = new DuoMap<>();
  private final DuoMap<Integer, String> _frequentNameIdMap = new DuoMap<>();

  private static Integer _id = 0;
  private final Tree tree = new Tree();
  private boolean flag = false;
  private boolean lineFlag = false;
  private Subject subject;
  private final List<List<Integer>> _allNumberPresentation = new LinkedList<>();
  private final List<List<Integer>> _allSequences = new LinkedList<>();
  private final Set<Patch> oriPatches = new HashSet<>();
  private final TokensProcessor tkp;
  private List<Patch> sortedPatches;


  public List<Patch> getSortedPatches() {
    return sortedPatches;
  }

  public FragmentProcessor(TokensProcessor tkp) {
    this.tkp = tkp;
  }

  public Set<Patch> getOriPatches() {
    return oriPatches;
  }

  public DuoMap<Integer, String> getFrequenciesName() {
    return _frequentNameIdMap;
  }

  public DuoMap<Integer, Fragment> getFrequencies() {
    return _frequentIdMap;
  }

  public List<List<Integer>> getAllSequences() {
    return _allSequences;
  }

  public void initFrequencies(CompilationUnit cu) {
    StatementProcessor skp = new StatementProcessor();
    skp.collectCu(cu);
    Identifier stmtIdentifier = skp.getIdentifier();
    stmtIdentifier.collectFrequent();
    collect(stmtIdentifier.getFrequentMap());

    ExprProcessor exp = new ExprProcessor();
    exp.collectCu(cu);
    Identifier expIdentifier = exp.getIdentifier();
    expIdentifier.collectFrequent();
    collect(expIdentifier.getFrequentMap());

//    Map<Integer, Fragment> statementMap = stmtIdentifier.getIdMap();
//    ExprProcessor exp = new ExprProcessor();
//    for (Entry<Integer, Fragment> entry : statementMap.entrySet()) {
//      exp.collectCu(entry.getValue().getNodeType());
//    }
//    Identifier expIdentifier = exp.getIdentifier();
//    expIdentifier.collectFrequent();

    //  Map<Integer, Fragment> expressionMap = expIdentifier.getIdMap();
//    TokenProcessorFre tkp = new TokenProcessorFre();
//    Identifier tokensIdentifier = tkp.getIdentifier();
//    for (Entry<Integer, Fragment> entry : expressionMap.entrySet()) {
//      tkp.collectCu(entry.getValue().getNodeType());
//    }
    //  tokensIdentifier.collectFrequent();
//
//    collect(stmtIdentifier.getFrequentMap());
//    collect(expIdentifier.getFrequentMap());
    // collect(tokensIdentifier.getFrequentMap());
  }

  public void collect(Map<Integer, Fragment> map) {
    for (Entry<Integer, Fragment> entry : map.entrySet()) {
//      if (!(_nameIdMap.containsValue(entry.getValue().getNodeType().toString()))) {
//        _idMap.put(entry.getKey(), entry.getValue());
//        _nameIdMap.put(entry.getKey(), entry.getValue().getNodeType().toString());
//      }
      if (!(_frequentNameIdMap.containsValue(entry.getValue().getNodeType().toString()))) {
        _frequentIdMap.put(_id, entry.getValue());
        _frequentNameIdMap.put(_id, entry.getValue().getNodeType().toString());
        _id++;
      }
    }
  }


  public void generateSequence(ASTNode node) {
    if (node == null) {
      return;
    }
    MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
    node.accept(methodVisitor);


  }

//  public enum MatchType {
//    previous,
//    rear,
//    middle,
//
//  }

  public MatchType findPos(BuggyFile buggyFile, int line, int insertPos, List<Integer> fixSeq) {
    //fix list
    MatchType type = middle;
    CompilationUnit cu = buggyFile.getCompilationUnit();
    CodeSearch codeSearch = new CodeSearch(cu, line, 1);
    ASTNode bugLine = codeSearch.getExtendedStatement();
    generateSequence(bugLine);
    lineFlag = true;
    List<Integer> numSeq = new ArrayList<Integer>(_allNumberPresentation.get(0));
    int firstPos = fixSeq.indexOf(numSeq.get(0));
    int lastPos = fixSeq.indexOf(numSeq.get(numSeq.size() - 1));
    if (insertPos < firstPos) {
      type = previous;
    } else if (insertPos > lastPos) {
      type = MatchType.rear;
    }
    return type;
  }

  public List<Pair<Integer, List<Integer>>> splitBug(List<Integer> oriSeq) {
    //pair 替换前 & 替换后
    //just split MI's name & args
    List<Pair<Integer, List<Integer>>> ans = new ArrayList<>();
    //ans[0] 最终结果 1..2  中间替换的序列
    List<Integer> result = new ArrayList<>(oriSeq);
    ans.add(0, new Pair<>(0, result));
    for (Integer x : oriSeq) {
      MethodInvocation method = null;
      if (_frequentIdMap.containsKey(x)) {
        ASTNode node = _frequentIdMap.getValue(x).getNodeType();
        if (node instanceof ExpressionStatement) {
          Expression exp = ((ExpressionStatement) node).getExpression();
          if (exp instanceof MethodInvocation) {
            method = (MethodInvocation) exp;
          }
        } else if (node instanceof MethodInvocation) {
          method = (MethodInvocation) node;
        }
      }
      if (method != null && !method.arguments().isEmpty()) {
        int index = result.indexOf(x);
        result.remove(x);
        //替换后
        List<Integer> replace = new ArrayList<>();
        if (method.getExpression() != null) {
          if (_frequentNameIdMap.containsValue(method.getExpression().toString())) {
            result.add(index++, _frequentNameIdMap.getKey(method.getExpression().toString()));
            replace.add(_frequentNameIdMap.getKey(method.getExpression().toString()));
          }
        }
        if (method.getName() != null) {
          if (_frequentNameIdMap.containsValue(method.getName().toString())) {
            result.add(index++, _frequentNameIdMap.getKey(method.getName().toString()));
            replace.add(_frequentNameIdMap.getKey(method.getName().toString()));
          }
        }
        //add args one by one
        if (method.arguments() != null) {
          for (Object arg : method.arguments()) {
            if (_frequentNameIdMap.containsValue(arg.toString())) {
              result.add(index++, _frequentNameIdMap.getKey(arg.toString()));
              replace.add(_frequentNameIdMap.getKey(arg.toString()));
            }
          }
        }
        ans.add(new Pair<>(x, replace));
      }
    }

    ans.get(0).setSecond(result);
    return ans;
  }


  public List<Integer> splitSequence(Block bugBlock) {
    flag = true;
    generateSequence(bugBlock);

    List<Integer> bugNumPresentation = new ArrayList<>(_allNumberPresentation.get(0));
    flag = false;
    //split
    List<Pair<Integer, List<Integer>>> splitResult = splitBug(bugNumPresentation);
    List<Integer> splitBugNumPresentation = splitResult.get(0).getSecond();

//    splitSequence(splitResult);
    //allSequence库里的序列对应也要拆开
    //从1开始，因为pair[0]是最终结果
    // List<List<Integer>> ans = new ArrayList<>();
    for (List<Integer> sequence : _allSequences) {
      for (int i = 1; i < splitResult.size(); i++) {
        for (int j = 0; j < sequence.size(); j++) {
          if (sequence.get(j).equals(splitResult.get(i).getFirst())) {
            sequence.remove(j);
            //    j--;
            sequence.addAll(j, splitResult.get(i).getSecond());
            j += splitResult.get(i).getSecond().size();
          }
        }
      }
    }

/*
    StringBuffer sb = new StringBuffer();
    if (bugBlock == null) {
      return null;
    }
    sb.append(bugBlock.toString() + "\n");
    sb.append("===========bugNumPresentation=======\n");
    for (Integer x : bugNumPresentation) {
      sb.append(_frequenciesName.getValue(x) + "(" + x + "), ");
    }
    sb.append("\n");
    sb.append("=========splitBugNumPresentation==========\n");
    for (Integer x : splitBugNumPresentation) {
      sb.append(_frequenciesName.getValue(x) + "(" + x + "), ");
    }
    JavaFile.writeStringToFile("/Users/yezizhi/Desktop/log/bugBlock.txt", sb.toString());
*/
    return splitBugNumPresentation;
  }


  public boolean isValidVar(Integer element, Set<String> valid) {
    boolean flag = true;
    if (!_frequentNameIdMap.containsKey(element)) {
      //不频繁
      flag = false;
    } else {
      ASTNode node = _frequentIdMap.getValue(element).getNodeType();
      //simpleName 无法插入
      TokenProcessorFre tkp = new TokenProcessorFre();
      ExprProcessor exp = new ExprProcessor();
      if (node instanceof SimpleName || node instanceof NullLiteral
          || node instanceof NumberLiteral || node instanceof BooleanLiteral ||
          node instanceof StringLiteral || node instanceof CharacterLiteral) {
        return false;
      } else if (node instanceof MethodInvocation) {
        List<String> vars = new LinkedList<>();
        ASTNode invoke = ((MethodInvocation) node).getExpression();
        if (invoke != null) {
          vars.add(invoke.toString());
        }

        if (((MethodInvocation) node).arguments().size() != 0) {
          for (Object arg : ((MethodInvocation) node).arguments()) {
            if (arg instanceof SimpleName) {
              vars.add(arg.toString());
            }
          }
        }
        for (String var : vars) {
          if (!valid.contains(var)) {
            return false;
          }
        }
      } else if (node instanceof ExpressionStatement
          && ((ExpressionStatement) node).getExpression() instanceof MethodInvocation) {
        Expression expr = ((ExpressionStatement) node).getExpression();
        List<String> vars = new LinkedList<>();
        ASTNode invoke = ((MethodInvocation) expr).getExpression();
        if (invoke != null) {
          vars.add(invoke.toString());
        }
        if (((MethodInvocation) expr).arguments().size() != 0) {
          for (Object arg : ((MethodInvocation) expr).arguments()) {
            if (arg instanceof SimpleName) {
              vars.add(arg.toString());
            }
          }
        }
        for (String var : vars) {
          if (!valid.contains(var)) {
            return false;
          }
        }
      } else if (node instanceof Statement) {
        exp.collectCu(node);
        for (ASTNode expression : exp.getNodeList()) {
          tkp.collectCu(expression);
        }
        for (String var : tkp.getNameList()) {
          if (!valid.contains(var)) {
            flag = false;
            break;
          }
        }
        tkp.clear();
      } else if (node instanceof Expression) {
        tkp.collectCu(node);
        for (String var : tkp.getNameList()) {
          if (!valid.contains(var)) {
            flag = false;
            break;
          }
        }
        tkp.clear();
      }

    }
    return flag;
  }

  public enum InsertType {
    invalid,//can't insert
    insertCondition,//插入的是一个条件
    //insertHead,//插入的位置是sequence第一个
    insert,//普通插入

  }

  public InsertType getInsertType(MatchType matchType, int e) {
    InsertType type = invalid;
    if (_frequentIdMap.containsKey(e)) {
      ASTNode target = _frequentIdMap.getValue(e).getNodeType();
      if (target instanceof Expression) {
        if (target.getParent() instanceof IfStatement) {
          if (matchType == previous || matchType == middle) {
            type = insertCondition;
          }
        } else if (target.getParent() instanceof Statement) {
          type = insert;
        }
      } else if (target instanceof Statement) {
        type = insert;
      }
    }
    return type;
  }


  private List<Patch> patternFix(MatchedPattern pattern, Pair<String, Integer> loc, Block bugBlock,
      BuggyFile buggyFile) {
    List<Patch> result = new LinkedList<>();
    List<Modification> modiList = new ArrayList<>();
    CompilationUnit cu = buggyFile.getCompilationUnit();
    Set<ASTNode> preCombine = new HashSet<>();
    if (bugBlock == null) {
      return null;
    }
    Modification modi = new Modification(buggyFile);
    List<Integer> bugNumPresentation = splitSequence(bugBlock);
    if (bugNumPresentation == null) {
      return null;
    }

    Set<Pair<Integer, MatchType>> fixed = new HashSet<>();

    //get fixed sequence
    for (Entry<Integer, List<Integer>> entry : pattern.getFixMap().entrySet()) {
      MatchType type = findPos(buggyFile, loc.getSecond(), entry.getKey(), bugNumPresentation);
      for (Integer now : entry.getValue()) {
        fixed.add(new Pair<>(now, type));
      }
    }

    String patternString = getStringPattern(pattern);

    for (Pair<Integer, MatchType> mm : fixed) {
      try {
        CodeSearch codeSearch = new CodeSearch(cu, loc.getSecond(), 1);
        ASTNode oriNode = codeSearch.getExtendedStatement();
        InsertType insertType = getInsertType(mm.getSecond(), mm.getFirst());
        if (_frequentIdMap.containsKey(mm.getFirst()) && insertType != invalid) {
          ASTNode target = _frequentIdMap.getValue(mm.getFirst()).getNodeType();
          int occurrences = pattern.getFrequency();

          if (mm.getSecond() == middle) {
            // replace
            preCombine.add(modi.replace(oriNode, target));
            Modification modi_before = new Modification(buggyFile);
            Modification modi_after = new Modification(buggyFile);
            preCombine.add(modi_before.insertBefore(oriNode, target, insertType));
            preCombine.add(modi_after.insertAfter(oriNode, target));
            modiList.add(modi);
            modiList.add(modi_before);
            modiList.add(modi_after);
          } else if (mm.getSecond() == previous) {
            //before
            preCombine.add(modi.insertBefore(oriNode, target, insertType));
            modiList.add(modi);

          } else if (mm.getSecond() == MatchType.rear) {
            //after
            preCombine.add(modi.insertAfter(oriNode, target));
            modiList.add(modi);

          }
          for (Modification m : modiList) {
            TextEdit edit = m.buildPatch();
            Patch patch = new Patch(edit, occurrences, buggyFile, PatchType.stmt_pattern,
                patternString);

            if (patch.getFixedString() != null) {
              oriPatches.add(patch);
            }
          }
          modiList.clear();
        }
      } catch (IllegalArgumentException e) {
      }
    }
    for (ASTNode pre : preCombine) {
      if (pre != null && pre.getLocationInParent() != null) {
        oriPatches.addAll(tkp.doSingleFix(pre, buggyFile, true, loc));
      }
    }
//    Pair<List<Modification>, Integer> result = new Pair<>();
//
//    if (occurrences == 0) {
//      return null;
//    } else {
//      result.setFirst(modiList);
//      result.setSecond(occurrences);
//      return result;
//    }

    return result;
  }

  private String getStringPattern(MatchedPattern pattern) {
    StringBuffer sb = new StringBuffer();
    for (Integer id : pattern.getPatterns()) {
      if (_frequentNameIdMap.containsKey(id)) {
        Fragment node = _frequentIdMap.getValue(id);
        String string = node.getNodeType().toString();
        int frequency = node.getTimes();
        String type = getNodeTypeString(node.getNodeType().getNodeType());
        //a(type)<frequency>,
        sb.append(string).append("(").append(type).append(")").append("<").append(frequency)
            .append(">").append(", ");
      }
    }
    return sb.toString();
  }

  private String getNodeTypeString(Integer nodeType) {
    switch (nodeType) {
      case 1:
        return "AnonymousClassDeclaration";
      case 2:
        return "ArrayAccess";
      case 3:
        return "ArrayCreation";
      case 4:
        return "ArrayInitializer";
      case 5:
        return "ArrayType";
      case 6:
        return "AssertStatement";
      case 7:
        return "Assignment";
      case 8:
        return "Block";
      case 9:
        return "BooleanLiteral";
      case 10:
        return "BreakStatement";
      case 11:
        return "CastExpression";
      case 12:
        return "CatchClause";
      case 13:
        return "CharacterLiteral";
      case 14:
        return "ClassInstanceCreation";
      case 15:
        return "CompilationUnit";
      case 16:
        return "ConditionalExpression";
      case 17:
        return "ConstructorInvocation";
      case 18:
        return "ContinueStatement";
      case 19:
        return "DoStatement";
      case 20:
        return "EmptyStatement";
      case 21:
        return "ExpressionStatement";
      case 22:
        return "FieldAccess";
      case 23:
        return "FieldDeclaration";
      case 24:
        return "ForStatement";
      case 25:
        return "IfStatement";
      case 26:
        return "ImportDeclaration";
      case 27:
        return "InfixExpression";
      case 28:
        return "Initializer";
      case 29:
        return "Javadoc";
      case 30:
        return "LabeledStatement";
      case 31:
        return "MethodDeclaration";
      case 32:
        return "MethodInvocation";
      case 33:
        return "NullLiteral";
      case 34:
        return "NumberLiteral";
      case 35:
        return "PackageDeclaration";
      case 36:
        return "ParenthesizedExpression";
      case 37:
        return "PostfixExpression";
      case 38:
        return "PrefixExpression";
      case 39:
        return "PrimitiveType";
      case 40:
        return "QualifiedName";
      case 41:
        return "ReturnStatement";
      case 42:
        return "SimpleName";
      case 43:
        return "SimpleType";
      case 44:
        return "SingleVariableDeclaration";
      case 45:
        return "StringLiteral";
      case 46:
        return "SuperConstructorInvocation";
      case 47:
        return "SuperFieldAccess";
      case 48:
        return "SuperMethodInvocation";
      case 49:
        return "SwitchCase";
      case 50:
        return "SwitchStatement";
      case 51:
        return "SynchronizedStatement";
      case 52:
        return "ThisExpression";
      case 53:
        return "ThrowStatement";
      case 54:
        return "TryStatement";
      case 55:
        return "TypeDeclaration";
      case 56:
        return "TypeDeclarationStatement";
      case 57:
        return "TypeLiteral";
      case 58:
        return "VariableDeclarationExpression";
      case 59:
        return "VariableDeclarationFragment";
      case 60:
        return "VariableDeclarationStatement";
      case 61:
        return "WhileStatement";
      case 62:
        return "InstanceofExpression";
      case 63:
        return "LineComment";
      case 64:
        return "BlockComment";
      case 65:
        return "TagElement";
      case 66:
        return "TextElement";
      case 67:
        return "MemberRef";
      case 68:
        return "MethodRef";
      case 69:
        return "MethodRefParameter";
      case 70:
        return "EnhancedForStatement";
      case 71:
        return "EnumDeclaration";
      case 72:
        return "EnumConstantDeclaration";
      case 73:
        return "TypeParameter";
      case 74:
        return "ParameterizedType";
      case 75:
        return "QualifiedType";
      case 76:
        return "WildcardType";
      case 77:
        return "NormalAnnotation";
      case 78:
        return "MarkerAnnotation";
      case 79:
        return "SingleMemberAnnotation";
      case 80:
        return "MemberValuePair";
      case 81:
        return "AnnotationTypeDeclaration";
      case 82:
        return "AnnotationTypeMemberDeclaration";
      case 83:
        return "Modifier";
      case 84:
        return "UnionType";
      case 85:
        return "Dimension";
      case 86:
        return "LambdaExpression";
      case 87:
        return "IntersectionType";
      case 88:
        return "NameQualifiedType";
      case 89:
        return "CreationReference";
      case 90:
        return "ExpressionMethodReference";
      case 91:
        return "SuperMethodReference";
      case 92:
        return "TypeMethodReference";
      default:
        throw new IllegalArgumentException();
    }
  }

  private void mutateFix(BuggyFile buggyFile, Pair<String, Integer> loc) {
    CompilationUnit cu = buggyFile.getCompilationUnit();
    CodeSearch testSearch = new CodeSearch(cu, loc.getSecond(), 1);
    ASTNode rear = testSearch.getExtendedStatement();
    if (rear == null) {
      return;
    }
    FragmentMutate mutator = new FragmentMutate(buggyFile, loc, rear);
    oriPatches.addAll(mutator.mutateFix());
  }


  public void doFix(Subject subject, Pair<String, Integer> loc) {
    oriPatches.clear();
    this.subject = subject;
//    Group.buildMap(subject);
//    List<Pair<String, Integer>> locations = subject.getAbstractFaultlocalization()
//        .getLocations(200);
//      for (Pair<String, Integer> loc : locations) {
//        fixEachLoc(loc);
//      }

    fixLine(loc);
    //BuggyFile buggyFile = BuggyFile.getBuggyFile(subject, loc);
    //mutateFix(buggyFile, loc);
    sortedPatches = new ArrayList<>(oriPatches);
    Collections.sort(sortedPatches);

//      if (TestPatch.getNumOfPass() >= 5) {
//        //TestPatch.endTest(subject);
//      return;
//      }
//      testFix(oriPatches, loc);
//    oriPatches.clear();
    // }

  }

//  private void doLineFix() {
//
//    List<Pair<String, Integer>> locations =
//        subject.getAbstractFaultlocalization().getLocations(200);
//
//    for (Pair<String, Integer> loc : locations) {
//       fixLine(loc);
////      BuggyFile buggyFile = new BuggyFile(subject, loc);
////      mutateFix(buggyFile, loc);
//      if (TestPatch.getNumOfPass() >= 5) {
//        TestPatch.endTest(subject);
//        return;
//      }
//      testFix(oriPatches, loc);
//      oriPatches.clear();
//    }
//
//  }


  private List<MatchedPattern> doMining(Pair<String, Integer> loc) {
    ShowInfo showInfo = new ShowInfo(this);

    BuggyFile buggyFile = BuggyFile.getBuggyFile(subject, loc);

    CompilationUnit cu = buggyFile.getCompilationUnit();

    FragmentBugFinder bugFinder = new FragmentBugFinder(loc.getSecond(), cu);

    //get bugBlock & bug sequence
    Block bugBlock = bugFinder.getBuggyBlock(buggyFile.getFilePath());

    List<Integer> splitBugNumPresentation = splitSequence(bugBlock);
    if (splitBugNumPresentation == null) {
      return null;
    }
    Set<String> validVar = bugFinder.getAvailableVars(buggyFile.getFilePath());
    //collectSimilarCode(validVar, bugFinder);

    //print frequent
    showInfo.showFrequent();
    showInfo.showAllSequences();
    //mining
    for (List<Integer> list : _allSequences) {
      List<Queue<Integer>> result = new LinkedList<>();
      Builder.buildSeqs(result, list, 2);
      Tree mirror = new Tree();
      result.forEach(q -> tree.build(q, mirror));
    }

    List<MatchedPattern> patterns = tree.do_dfs(splitBugNumPresentation);
   FragmentMatcher matcher = new FragmentMatcher(bugBlock, splitBugNumPresentation);

    //print all patterns
    //showInfo.showPatterns(patterns);

    //match
    //get the context of buggy and get valid variables,filter invalid ones
    List<MatchedPattern> matchedPatterns = new ArrayList<>();
    for (MatchedPattern candidate : patterns) {
      Map<Integer, List<Integer>> insertPre = matcher.tryInsert(candidate,
          splitBugNumPresentation.size(), _frequentIdMap.getKv());
      Map<Integer, List<Integer>> insertResult = new HashMap<>(matcher.tryInsert(candidate,
          splitBugNumPresentation.size(), _frequentIdMap.getKv()));
      if (!insertPre.isEmpty()) {
        for (Entry<Integer, List<Integer>> entry : insertPre.entrySet()) {
          Integer key = entry.getKey();
          insertResult.get(key).removeIf(element -> !isValidVar(element, validVar));
          if (insertResult.get(key).isEmpty()) {
            insertResult.remove(key);
          }
        }
        if (!insertResult.isEmpty()) {
          candidate.setFixMap(insertResult);
          matchedPatterns.add(candidate);
          //matchedPatterns.add(insertResult);
        }
      }
    }

    // showInfo.showMatchedPattern(matchedPatterns);
    return matchedPatterns;
  }

  private Set<Map<Integer, List<Integer>>> matchedPattern2FixMap(Set<MatchedPattern> patterns) {
    Set<Map<Integer, List<Integer>>> maps = new HashSet<>();
    for (MatchedPattern p : patterns) {
      if (p.getFixMap() != null) {
        maps.add(p.getFixMap());
      }
    }
    return maps;
  }

  private void fixLine(Pair<String, Integer> loc) {
    // List<Pair<String, Integer>> group = Group.checkLine(loc);
    List<MatchedPattern> matchedPatterns = doMining(loc);
    if (matchedPatterns != null) {
//      sort(matchedPatterns);
      BuggyFile buggyFile = BuggyFile.getBuggyFile(subject, loc);
      FragmentBugFinder bugFinder = new FragmentBugFinder(loc.getSecond(),
          buggyFile.getCompilationUnit());
      for (MatchedPattern pattern : matchedPatterns) {
        List<Patch> fixResult = patternFix(pattern, loc,
            bugFinder.getBuggyBlock(buggyFile.getFilePath()), buggyFile);
        if (fixResult != null) {
          oriPatches.addAll(fixResult);
//          Integer occurrences = patternFix(pattern, loc).getSecond();
//          List<Modification> modiList = patternFix(pattern, loc).getFirst();
//          for (Modification modi : modiList) {
//            TextEdit edit = modi.buildPatch();
//            Patch patch = new Patch(edit, occurrences, 0, buggyFile, PatchType.stmt_pattern);
//            if (patch.getFixedString() != null) {
//              oriPatches.add(patch);
//            }
        }
//        if (group != null) {
//          sysPattern(group, pattern);
//        }
      }
    }
  }

//  private void sysPattern(List<Pair<String, Integer>> group, Map<Integer, List<Integer>> pattern) {
//    BuggyFile buggyFile = new BuggyFile(subject, group.get(0));
//    Pair<List<Modification>, Integer> p = patternFix(pattern, group.get(0));
//    Modification modi = p.getFirst().get(0);
//    Integer occurrences = p.getSecond();
//    TextEdit edit = modi.buildPatch();
//    for (int i = 1; i < group.size(); i++) {
//      Pair<String, Integer> applyLoc = group.get(i);
//      Modification applyModi = patternFix(pattern, applyLoc).getFirst().get(0);
//      edit.addChild(applyModi.buildPatch());
//    }
//    Patch patch = new Patch(edit, occurrences, 1, buggyFile, PatchType.sys);
//    if (patch.getFixedString() != null) {
//      oriPatches.add(patch);
//    }
//  }

//  public void testFix(Set<Patch> oriPatches, Pair<String, Integer> loc) {
//    TestPatch testPatch = new TestPatch();
//    testPatch.test(oriPatches, loc, subject);
//  }


  public class MethodDeclarationVisitor extends ASTVisitor {

    private List<Integer> _sequence = new LinkedList<>();
    List<Integer> _numberPresentation = new LinkedList<>();

    public boolean visit(MethodDeclaration node) {
      FragmentVisitor fragmentVisitor = new FragmentVisitor();
      node.accept(fragmentVisitor);
      //todo :split
      _allSequences.addAll(findChildren(null, _sequence));
      //  _allSequences.add(_sequence);
      _sequence = new LinkedList<>();
      return true;
    }

    public boolean visit(Block node) {
      if (flag) {
        FragmentVisitor fragmentVisitor = new FragmentVisitor();
        node.accept(fragmentVisitor);
        _allNumberPresentation.add(_numberPresentation);
        _numberPresentation = new LinkedList<>();
      }
      return true;
    }

    public boolean visit(Statement node) {
      if (lineFlag) {
        FragmentVisitor fragmentVisitor = new FragmentVisitor();
        node.accept(fragmentVisitor);
        _allNumberPresentation.add(_numberPresentation);
        _numberPresentation = new LinkedList<>();
      }
      return true;
    }

    public class FragmentVisitor extends ASTVisitor {

      public boolean visit(IfStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(ReturnStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(ForStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(ExpressionStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(EnhancedForStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(DoStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(WhileStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(VariableDeclarationStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(Block node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(BreakStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(ContinueStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(ThrowStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(LabeledStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }


      public boolean visit(VariableDeclarationFragment node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(SwitchStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(TryStatement node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }

      public boolean visit(SwitchCase node) {
        _sequence.addAll(process(node));
        _numberPresentation.addAll(process(node));
        return false;
      }


    }

    public List<Integer> process(Block block) {
      List<Integer> ans = new LinkedList<>();
      List stmt = block.statements();
      for (Object st : stmt) {
        ans.addAll(process((ASTNode) st));
      }
      return ans;
    }

//    public List<Integer> process(IfStatement stmt) {
//      List<Integer> ans = new LinkedList<>();
//      ans.addAll(process(stmt.getThenStatement()));
//      Statement elseStmt = stmt.getElseStatement();
//      if (elseStmt != null) {
//        ans.addAll(process(elseStmt));
//      }
//      return ans;
//    }


    public List<Integer> process(ASTNode node) {
      if (node instanceof Block) {
        return process((Block) node);
      }
      List<Integer> ans = new LinkedList<>();
      if (_frequentNameIdMap.containsValue(node.toString()) || _frequentNameIdMap.containsValue(
          node.toString() + "\n")) {
        //isFrequent
        ans.add(_frequentNameIdMap.getKey(node.toString()));
      } else {
        ans.addAll(processExp(node));
        ans.removeAll(Collections.singleton(null));
      }
//      for (Integer a : ans) {
//        if (a < 0) {
//          System.out.println("===================");
//        } else {
//          System.out.println(_frequentNameIdMap.getValue(a) + " ");
//        }
//      }

      return ans;
    }


    public List<List<Integer>> findChildren(List<Integer> head, List<Integer> ori) {
      ori.removeAll(Collections.singleton(null));
      List<List<Integer>> result = new ArrayList<>();
      //  int front;
      //  int rear = -1;
//      if(ori.size()==0){
//        List<Integer>temp=new ArrayList<>(head);
//        result.add(temp);
//      }
//      for (int i = 0; i < ori.size(); i++) {
//        if (ori.get(i) < 0) {
//          front = rear;
//          rear = i;
//          List<Integer> temp = new ArrayList<>();
//          if (head != null) {
//            temp.addAll(head);
//          }
//          temp.addAll(ori.subList(front + 1, rear));
//          temp.removeAll(Collections.singleton(null));
//          result.add(temp);
//        }
//      }

      List<Integer> temp = new ArrayList<>();
      int front = 0;
      int rear = ori.size();

      while (ori.subList(front, ori.size()).contains(-1)) {
        temp = new ArrayList<>();
        if (head != null) {
          temp.addAll(head);
        }
        rear = front + ori.subList(front, ori.size()).indexOf(-1);
        temp.addAll(ori.subList(front, rear));
        result.add(temp);
        front = rear + 1;
        if (front >= ori.size()) {
          break;
        }
      }
      //避免 3 4 -1 这样的序列被添加两次
      if (ori.size() != 0 && ori.get(ori.size() - 1) != -1) {
        temp = new ArrayList<>();
        if (head != null) {
          temp.addAll(head);
        }
        temp.addAll(ori.subList(front, ori.size()));
        result.add(temp);
      }
      return result;

    }


    public List<Integer> processExp(ASTNode node) {
      if (node instanceof ExpressionStatement) {
        return processExp((ExpressionStatement) node);
      } else if (node instanceof IfStatement) {
        return processExp((IfStatement) node);
      } else if (node instanceof ForStatement) {
        return processExp((ForStatement) node);
      } else if (node instanceof Block) {
        return processExp((Block) node);
      } else if (node instanceof BreakStatement) {
        return processExp((BreakStatement) node);
      } else if (node instanceof ContinueStatement) {
        return processExp((ContinueStatement) node);
      } else if (node instanceof DoStatement) {
        return processExp((DoStatement) node);
      } else if (node instanceof EnhancedForStatement) {
        return processExp((EnhancedForStatement) node);
      } else if (node instanceof ReturnStatement) {
        return processExp((ReturnStatement) node);
      } else if (node instanceof ThrowStatement) {
        return processExp((ThrowStatement) node);
      } else if (node instanceof LabeledStatement) {
        return processExp((LabeledStatement) node);
      } else if (node instanceof WhileStatement) {
        return processExp((WhileStatement) node);
      } else if (node instanceof VariableDeclarationFragment) {
        return processExp((VariableDeclarationFragment) node);
      } else if (node instanceof VariableDeclarationStatement) {
        return processExp((VariableDeclarationStatement) node);
      } else if (node instanceof SwitchStatement) {
        return processExp((SwitchStatement) node);
      } else if (node instanceof TryStatement) {
        return processExp((TryStatement) node);
      } else if (node instanceof InfixExpression) {
        return processExp((InfixExpression) node);
      } else if (node instanceof ArrayAccess) {
        return processExp((ArrayAccess) node);
      } else if (node instanceof ConditionalExpression) {
        return processExp((ConditionalExpression) node);
      } else if (node instanceof MethodInvocation) {
        return processExp((MethodInvocation) node);
      } else if (node instanceof ParenthesizedExpression) {
        return processExp((ParenthesizedExpression) node);
      } else if (node instanceof CastExpression) {
        return processExp((CastExpression) node);
      } else if (node instanceof PostfixExpression) {
        return processExp((PostfixExpression) node);
      } else if (node instanceof PrefixExpression) {
        return processExp((PrefixExpression) node);
      } else if (node instanceof SuperMethodInvocation) {
        return processExp((SuperMethodInvocation) node);
      } else if (node instanceof ClassInstanceCreation) {
        return processExp((ClassInstanceCreation) node);
      } else if (node instanceof ArrayCreation) {
        return processExp((ArrayCreation) node);
      } else if (node instanceof ArrayInitializer) {
        return processExp((ArrayInitializer) node);
      }

      List<Integer> ans = new LinkedList<>();

      if (node != null) {
        ans.add(getId(node, node.toString()));
      }
      return ans;
    }


    public List<Integer> processExp(ExpressionStatement exprStmt) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = exprStmt.getExpression();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        } else {
//          ans.addAll(processExp(expression));
//        }
        ans.addAll(nodeProcessExp(expression));
      }
      return ans;
    }

    public List<Integer> processExp(SwitchStatement switchStatement) {
      List<Integer> ans = new LinkedList<>();
      List<Integer> head = new ArrayList<>();
      List<List<Integer>> result = new ArrayList<>();
      Expression expression = switchStatement.getExpression();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          // ans.add(setId(expression, expression.toString()));
//          head.add(getId(expression, expression.toString()));
//        } else {
//          head.addAll(processExp(expression));
//        }
        head.addAll(nodeProcessExp(expression));
      }
      for (Object stmt : switchStatement.statements()) {
        List<Integer> temp = new ArrayList<>();
        temp.addAll(nodeProcessExp((ASTNode) stmt));
//        temp.addAll(processExp((ASTNode) stmt));
        temp.add(-1);
        result.addAll(findChildren(head, temp));
      }

      for (List<Integer> l : result) {
        ans.addAll(l);
        ans.add(-1);
      }
      return ans;
    }

    public List<Integer> processExp(IfStatement ifStatement) {
      List<Integer> ans = new LinkedList<>();
      List<List<Integer>> result = new ArrayList<>();
      List<Integer> head = new ArrayList<>();
      Expression expression = ifStatement.getExpression();

      head.addAll(nodeProcessExp(expression));
//      head.add(getId(expression, expression.toString()));

      Statement thenStatement = ifStatement.getThenStatement();
      Statement elseStatement = ifStatement.getElseStatement();
      List<Integer> thenAns = new ArrayList<>();
      thenAns.addAll(nodeProcessExp(thenStatement));
      thenAns.add(-1);
      result.addAll(findChildren(head, thenAns));

      if (ifStatement.getElseStatement() != null) {
        List<Integer> elseAns = new ArrayList<>();
        elseAns.addAll(nodeProcessExp(elseStatement));
//        if (isFrequent(elseStatement)) {
//          elseAns.add(getId(elseStatement, elseStatement.toString()));
//        } else {
//          elseAns.addAll(processExp(elseStatement));
//        }
        result.addAll(findChildren(head, elseAns));
      }

      for (List<Integer> l : result) {
        ans.addAll(l);
        ans.add(-1);
      }
      return ans;
    }

    public List<Integer> iteratorProcessExp(List list) {
      List<Integer> ans = new LinkedList<>();
      if (list.size() != 0) {
        for (Object e : list) {
          if (isFrequent((ASTNode) e)) {
            ans.add(getId((ASTNode) e, ((ASTNode) e).toString()));
          } else if (tokenFilter((ASTNode) e)) {
            ans.addAll(processExp((ASTNode) e));
          }
        }
      }
      return ans;
    }

    public List<Integer> nodeProcessExp(ASTNode node) {
      List<Integer> ans = new LinkedList<>();
      if (isFrequent(node)) {
        ans.add(getId(node, node.toString()));
      } else if (tokenFilter(node)) {
        ans.addAll(processExp((node)));
      }
      return ans;
    }

    public List<Integer> processExp(ForStatement forStatement) {
      List<Integer> ans = new LinkedList<>();
      List initial = forStatement.initializers();
      if (initial.size() != 0) {
//        for (Object init : initial) {
//          if (isFrequent((ASTNode) init) && tokenFilter((ASTNode) init)) {
//            ans.add(getId((ASTNode) init, ((ASTNode) init).toString()));
//          } else {
//            ans.addAll(processExp((ASTNode) init));
//          }
//        }
        ans.addAll(iteratorProcessExp(initial));
      }
      Expression expression = forStatement.getExpression();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        } else {
//          ans.addAll(processExp(expression));
//        }
        ans.addAll(nodeProcessExp(expression));
      }
      List updater = forStatement.updaters();
      if (updater.size() != 0) {
        ans.addAll(iteratorProcessExp(updater));
//        for (Object update : updater) {
//          if (isFrequent((ASTNode) update) && tokenFilter((ASTNode) update)) {
//            ans.add(getId((ASTNode) update, ((ASTNode) update).toString()));
//          } else {
//            ans.addAll(processExp((ASTNode) update));
//          }
//        }
      }
      Statement body = forStatement.getBody();
//      if (isFrequent(body)) {
//        ans.add(getId(body, body.toString()));
//      } else {
//        ans.addAll(processExp(forStatement.getBody()));
//      }
      ans.addAll(nodeProcessExp(body));
      return ans;
    }

    public List<Integer> processExp(EnhancedForStatement enhancedForStatement) {
      List<Integer> ans = new LinkedList<>();
      ASTNode parameter = enhancedForStatement.getParameter();
      ans.addAll(nodeProcessExp(parameter));
//      if (!isFrequent((enhancedForStatement.getParameter()))) {
//        ans.addAll(processExp(enhancedForStatement.getParameter()));
//      }
      Expression expression = enhancedForStatement.getExpression();
      if (expression != null) {
        ans.addAll(nodeProcessExp(expression));
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
      }
      Statement body = enhancedForStatement.getBody();
      ans.addAll(nodeProcessExp(body));
//      if (!isFrequent((enhancedForStatement.getBody()))) {
//        ans.addAll(processExp(enhancedForStatement.getBody()));
//      }
      return ans;
    }

    public List<Integer> processExp(Block block) {
      List<Integer> ans = new LinkedList<>();
      List stmt = block.statements();
      ans.addAll(iteratorProcessExp(stmt));
//      for (Object st : stmt) {
//        if (!isFrequent((ASTNode) st)) {
//          ans.addAll(processExp((ASTNode) st));
//        }
//      }
      return ans;
    }


    public List<Integer> processExp(BreakStatement breakStatement) {
      List<Integer> ans = new LinkedList<>();
      SimpleName label = breakStatement.getLabel();
      if (label != null) {
//        if (!isFrequent(label)) {
//          ans.addAll(processExp(label));
//        }
        ans.addAll(nodeProcessExp(label));
      }
      return ans;
    }

    public List<Integer> processExp(ContinueStatement continueStatement) {
      List<Integer> ans = new LinkedList<>();
      SimpleName label = continueStatement.getLabel();
      if (label != null) {
//        if (!isFrequent(label)) {
//          ans.addAll(processExp(label));
//        }
        ans.addAll(nodeProcessExp(label));
      }
      return ans;
    }

    public List<Integer> processExp(ReturnStatement returnStatement) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = returnStatement.getExpression();
      if (expression != null) {
        ans.addAll(nodeProcessExp(expression));
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
      }
      return ans;
    }


    public List<Integer> processExp(DoStatement doStatement) {
      List<Integer> ans = new LinkedList<>();
      Statement body = doStatement.getBody();
//      if (!isFrequent(doStatement.getBody())) {
//        ans.addAll(processExp(doStatement.getBody()));
//      }
      ans.addAll(nodeProcessExp(body));
      Expression expression = doStatement.getExpression();
      if (expression != null) {
        ans.addAll(nodeProcessExp(expression));
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
      }
      return ans;
    }

    public List<Integer> processExp(ThrowStatement throwStatement) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = throwStatement.getExpression();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(throwStatement.getExpression())) {
//          ans.addAll(processExp(throwStatement.getExpression()));
//        }
        ans.addAll(nodeProcessExp(expression));
      }
      return ans;
    }

    public List<Integer> processExp(LabeledStatement labeledStatement) {
      List<Integer> ans = new LinkedList<>();
      SimpleName label = labeledStatement.getLabel();
      if (label != null) {
//        if (!isFrequent(label)) {
//          ans.addAll(processExp(label));
//        }
        ans.addAll(nodeProcessExp(label));
      }
      Statement body = labeledStatement.getBody();
      ans.addAll(nodeProcessExp(body));
//      if (!isFrequent(labeledStatement.getBody())) {
//        ans.addAll(processExp(labeledStatement.getBody()));
//      }
      return ans;
    }

    public List<Integer> processExp(WhileStatement whileStatement) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = whileStatement.getExpression();
      Statement body = whileStatement.getBody();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
        ans.addAll(nodeProcessExp(expression));
      }
      ans.addAll(nodeProcessExp(body));
//      if (!isFrequent(whileStatement.getBody())) {
//        ans.addAll(processExp(whileStatement.getBody()));
//      }
      return ans;
    }


    public List<Integer> processExp(VariableDeclarationStatement variableDeclarationStmt) {
      List<Integer> ans = new LinkedList<>();

      List modifiers = variableDeclarationStmt.modifiers();

      for (Object modifier : modifiers) {
        if (modifier instanceof
            SingleMemberAnnotation) {
          continue;
        }
        ans.addAll(nodeProcessExp((Modifier) modifier));
//        if (!isFrequent((Modifier) modifier)) {
//          ans.addAll(processExp((Modifier) modifier));
//        }
      }
      variableDeclarationStmt.getType();
      List fragments = variableDeclarationStmt.fragments();
      ans.addAll(iteratorProcessExp(fragments));
//      if (fragments.size() != 0) {
//        for (Object fragment : fragments) {
//          if (!isFrequent((VariableDeclarationFragment) fragment)) {
//            ans.addAll(processExp((VariableDeclarationFragment) fragment));
//          }
//        }
//      }
      return ans;
    }


    public List<Integer> processExp(VariableDeclarationFragment variableDeclarationFragment) {
      List<Integer> ans = new LinkedList<>();
      Expression initializer = variableDeclarationFragment.getInitializer();
      if (initializer != null) {
        ans.addAll(nodeProcessExp(initializer));
//        if (!isFrequent(initializer)) {
//          ans.addAll(processExp(initializer));
//        }
//        if (!isLeafNode(initializer)) {
//          ans.add(getId(initializer, initializer.toString()));
//        }
      }
      return ans;
    }


    public List<Integer> processExp(TryStatement tryStatement) {
      List<Integer> ans = new LinkedList<>();
      Statement body = tryStatement.getBody();
      Block finalBlock = tryStatement.getFinally();
      ans.addAll(nodeProcessExp(body));
//      if (!isFrequent(tryStatement.getBody())) {
//        ans.addAll(processExp(tryStatement.getBody()));
//      }
      if (tryStatement.getFinally() != null) {
//        if (!isFrequent(tryStatement.getFinally())) {
//          ans.addAll(processExp(tryStatement.getFinally()));
//        }
        ans.addAll(nodeProcessExp(finalBlock));
      }
      return ans;
    }

    public List<Integer> processExp(CastExpression castExpr) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = castExpr.getExpression();
      if (expression != null) {
        ans.addAll(nodeProcessExp(expression));
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
      }
      return ans;
    }


    public List<Integer> processExp(ClassInstanceCreation classInstanceCreation) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = classInstanceCreation.getExpression();
      if (expression != null) {
        ans.addAll(nodeProcessExp(expression));
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
      }
      List args = classInstanceCreation.arguments();
      ans.addAll(iteratorProcessExp(args));
//      if (args.size() != 0) {
//        for (Object arg : args) {
//          if (!isFrequent((Expression) arg)) {
//            ans.addAll(processExp((Expression) arg));
//          }
//        }
//
//      }
      return ans;
    }

    public List<Integer> processExp(InfixExpression infixExpr) {
      List<Integer> ans = new LinkedList<>();
      Expression left = infixExpr.getLeftOperand();
      Expression right = infixExpr.getRightOperand();
      if (left != null) {
//        if (!isFrequent(left)) {
//          ans.addAll(processExp(left));
        ans.addAll(nodeProcessExp(left));
      }
//        if (!isLeafNode(left)) {
//          ans.add(getId(left, left.toString()));
//        }
//      }
      if (right != null) {
        ans.addAll(nodeProcessExp(right));
//        if (!isFrequent(right)) {
//          ans.addAll(processExp(right));
//        }
//        if (!isLeafNode(right)) {
//          ans.add(getId(right, right.toString()));
//        }
      }
      return ans;
    }

    public List<Integer> processExp(ArrayAccess arrayAccess) {
      List<Integer> ans = new LinkedList<>();
      Expression array = arrayAccess.getArray();
      Expression index = arrayAccess.getIndex();
      if (array != null) {
//        if (!isFrequent(array)) {
//          ans.addAll(processExp(array));
//        }
//        if (!isLeafNode(array)) {
//          ans.add(getId(array, array.toString()));
//        }
        ans.addAll(nodeProcessExp(array));
      }
      if (index != null) {
//        if (!isFrequent(index)) {
//          ans.addAll(processExp(index));
//        }
//        if (!isLeafNode(index)) {
//          ans.add(getId(index, index.toString()));
//        }
        ans.addAll(nodeProcessExp(index));
      }
      return ans;
    }

    public List<Integer> processExp(ConditionalExpression conditionalExpression) {
      List<Integer> ans = new LinkedList<>();
      // max=(a>b)?a:b;
      Expression expression = conditionalExpression.getExpression();
      Expression thenExpression = conditionalExpression.getThenExpression();
      Expression elseExpression = conditionalExpression.getElseExpression();
      if (expression != null) {
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
        ans.addAll(nodeProcessExp(expression));
      }
      if (thenExpression != null) {
//        if (!isFrequent(thenExpression)) {
//          ans.addAll(processExp(thenExpression));
//        }
//        if (!isLeafNode(thenExpression)) {
//          ans.add(getId(thenExpression, thenExpression.toString()));
//        }
        ans.addAll(nodeProcessExp(thenExpression));
      }
      if (elseExpression != null) {
//        if (!isFrequent(elseExpression)) {
//          ans.addAll(processExp(elseExpression));
//        }
//        if (!isLeafNode(elseExpression)) {
//          ans.add(getId(elseExpression, elseExpression.toString()));
//        }
        ans.addAll(nodeProcessExp(elseExpression));
      }
      return ans;
    }


    public List<Integer> processExp(MethodInvocation methodInvocation) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = methodInvocation.getExpression();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
        ans.addAll(nodeProcessExp(expression));
      }
      Expression name = methodInvocation.getName();
//      if (!isLeafNode(name)) {
//        ans.add(getId(name, name.toString()));
//      }
//      if (!isFrequent(name)) {
//        ans.addAll(processExp(name));
//      }
      ans.addAll(nodeProcessExp(name));
      List arguments = methodInvocation.arguments();
//      if (arguments.size() != 0) {
//        for (Object arg : methodInvocation.arguments()) {
//          if (arg instanceof Expression) {
//            if (!isFrequent((Expression) arg)) {
//              ans.addAll(processExp((Expression) arg));
//            }
//            if (!isLeafNode((Expression) arg)) {
//              ans.add(getId((Expression) arg, arg.toString()));
//            }
//          }
//        }
//      }
      ans.addAll(iteratorProcessExp(arguments));
      return ans;
    }

    public List<Integer> processExp(ParenthesizedExpression parenthesizedExpression) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = parenthesizedExpression.getExpression();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
        ans.addAll(nodeProcessExp(expression));
      }
      return ans;
    }

    public List<Integer> processExp(PostfixExpression postfixExpression) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = postfixExpression.getOperand();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(expression)) {
//          ans.addAll(processExp(expression));
//        }
        ans.addAll(nodeProcessExp(expression));
      }
      return ans;
    }

    public List<Integer> processExp(PrefixExpression prefixExpression) {
      List<Integer> ans = new LinkedList<>();
      Expression expression = prefixExpression.getOperand();
      if (expression != null) {
//        if (isFrequent(expression) && tokenFilter(expression)) {
//          ans.add(getId(expression, expression.toString()));
//        }
//        if (!isFrequent(prefixExpression.getOperand())) {
//          ans.addAll(processExp(prefixExpression.getOperand()));
//        }
        ans.addAll(nodeProcessExp(expression));
      }
      return ans;
    }


    public List<Integer> processExp(SuperMethodInvocation superMethodInvocation) {
      List<Integer> ans = new LinkedList<>();
      List arguments = superMethodInvocation.arguments();
      //todo:name
//      if (arguments.size() != 0) {
//        for (Object arg : superMethodInvocation.arguments()) {
//          if (arg instanceof Expression) {
//            if (!isFrequent((Expression) arg)) {
//              ans.addAll(processExp((Expression) arg));
//            }
//            if (!isLeafNode((Expression) arg)) {
//              ans.add(getId((Expression) arg, arg.toString()));
//            }
//          }
//        }
//      }
      ans.addAll(iteratorProcessExp(arguments));
      return ans;
    }

    public List<Integer> processExp(ArrayCreation arrayCreation) {
      List<Integer> ans = new LinkedList<>();
      List dimension = arrayCreation.dimensions();
//      if (dimension.size() != 0) {
//        for (Object dim : dimension) {
//          if (dim instanceof Expression) {
//            if (!isFrequent((Expression) dim)) {
//              ans.addAll(processExp((Expression) dim));
//            }
//            if (!isLeafNode((Expression) dim)) {
//              ans.add(getId((Expression) dim, dim.toString()));
//            }
//          }
//        }
//      }
      ans.addAll(iteratorProcessExp(dimension));
      ArrayInitializer initializer = arrayCreation.getInitializer();
      if (initializer != null) {
//        if (!isFrequent(arrayCreation.getInitializer())) {
//          ans.addAll(processExp(arrayCreation.getInitializer()));
//        }
//        if (!isLeafNode(arrayCreation)) {
//          ans.add(getId(arrayCreation, arrayCreation.toString()));
//        }
        ans.addAll(nodeProcessExp(initializer));
      }
      return ans;
    }

    public List<Integer> processExp(ArrayInitializer arrayInitial) {
      List<Integer> ans = new LinkedList<>();
      List args = arrayInitial.expressions();
      ans.addAll(iteratorProcessExp(args));
//      if (args.size() != 0) {
//        for (Object arg : args) {
//          if (arg instanceof Expression) {
//            if (!isFrequent((Expression) arg)) {
//              ans.addAll(processExp((Expression) arg));
//            }
//            if (!isLeafNode((Expression) arg)) {
//              ans.add(getId((Expression) arg, arg.toString()));
//            }
//          }
//        }
//      }
      return ans;
    }

  }

  public Integer getId(ASTNode node, String string) {
    if (_frequentNameIdMap.containsValue(string) || _frequentNameIdMap.containsValue(
        string + '\n')) {
      Integer id = _frequentNameIdMap.getKey(string);
      Fragment fragment = _frequentIdMap.getValue(id);
      // fragment.setTimes();
      return id;
    } else if (_frequentNameIdMap.containsValue(string + '(') && isMethodName(node)) {
      Integer id = _frequentNameIdMap.getKey(string + "(");
      Fragment fragment = _frequentIdMap.getValue(id);
      // fragment.setTimes();
      return id;
    } else {
//      if (tokenFilter(node)) {
//        if (isMethodName(node)) {
//          _nameIdMap.put(_id, string + "(");
//        } else {
//          _nameIdMap.put(_id, string);
//        }
//        Fragment fragment = new Fragment(node);
//        fragment.setTimes();
//        _idMap.put(_id, fragment);
//        return _id++;
//      }
      return null;
    }
//    return null;
  }

  public boolean tokenFilter(ASTNode node) {
    if (node instanceof NumberLiteral || node instanceof StringLiteral ||
        node instanceof BooleanLiteral || node instanceof CharacterLiteral
        || node instanceof NullLiteral) {
      return false;
    }
    if (node instanceof SimpleName) {
      if (isCondition(node) || isMethodName(node) || isSwitchExpression(node)) {
        return true;
      } else {
        return false;
      }
    }
    return true;
  }

  public boolean isMethodName(ASTNode node) {
    if (node.getParent() instanceof MethodInvocation && node.getLocationInParent().getId()
        .equals("name")) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isSwitchExpression(ASTNode node) {
    if (node.getParent() instanceof SwitchStatement && node.getLocationInParent().getId()
        .equals("expression")) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isCondition(ASTNode node) {
    if (node.getParent() instanceof IfStatement && node.getLocationInParent().getId()
        .equals("expression")) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isLeafNode(ASTNode node) {
    if (node instanceof NumberLiteral || node instanceof StringLiteral ||
        node instanceof BooleanLiteral || node instanceof CharacterLiteral
        || node instanceof NullLiteral || node instanceof SimpleName ||
        node instanceof QualifiedName) {
      return true;
    } else {
      return false;
    }
  }

//public List<Integer>filterFrequent(List<Integer> ori){
//   for(Integer i:ori){
//     if(_idMap.getKv().get(i).getTimes()<2){
//
//     }
//   }
//}

  public boolean isFrequent(ASTNode node) {
    String string = node.toString();
    if (_frequentNameIdMap.containsValue(string) || _frequentNameIdMap.containsValue(
        string + '\n')) {
      return true;
    } else if (_frequentNameIdMap.containsValue(string + '(') && isMethodName(node)) {
      return true;
    } else {
      return false;
    }
  }

  private List<Block> collectSimilarCode(Set<String> validVar, FragmentBugFinder finder) {
    List<String> message = new ArrayList<>();
    List<String> tokens = new ArrayList<>(validVar);
    for (String t : tokens) {
      String cmd = Constant.COMMAND_CD + subject.getHome() + subject.getSsrc()
          + "&& grep -n -r -v --include \"*.java\" -E \"^\\s*/\\*|^\\s*//|^\\s*\\*\"  |"
          + "grep \"\\b" + t + "\\b\"";
      message.addAll(Executor.execute(new String[]{"/bin/bash", "-c", cmd}));
    }
//    Set<Pair> locs = new HashSet<>();
//    Set<Block>blocks = new HashSet<>();
//    for (String m : message) {
//      Pair<String, Integer> l = new Pair<>();
//      l.setFirst(m.split(":")[0]);//subject.getHome()+ subject.getSsrc() remove .
//      l.setSecond(Integer.valueOf(m.split(":")[1]));
//      locs.add(l);
//     blocks.add(finder.getSimilarBlock(l.getFirst(),l.getSecond()));
//    }

//    Set<Block> blocks = new HashSet<>();
    List<Block> blocks = new ArrayList<>();
    Block testBlock = null;
    for (String m : message) {
      String file = subject.getHome() + subject.getSsrc() + m.split(":")[0].replace(".", "");
      Integer line = Integer.valueOf(m.split(":")[1]);
      blocks.add(finder.getSimilarBlock(file, line));

    }
    try {

      FileOutputStream fileOut =
          new FileOutputStream("/tmp/employee.ser");
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(blocks.get(2).getAST());
      out.close();
      fileOut.close();
      System.out.printf("Serialized data is saved in /tmp/employee.ser");
    } catch (IOException i) {
      i.printStackTrace();
    }
    return blocks;
  }
}




