package cofix.core.preprocess.tokenRepair;

import cofix.common.util.Pair;
import cofix.common.util.Traid;
import cofix.core.modification.BuggyFile;
import cofix.core.modification.EventList;
import cofix.core.modification.Modification;
import cofix.core.modification.ModificationEvent;
import cofix.core.modification.ParamInsertEvent;
import cofix.core.pattern.MatchedPattern;
import cofix.core.preprocess.Patch;
import cofix.core.preprocess.Patch.PatchType;
import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.NullLiteralToken;
import cofix.core.preprocess.token.WrongPatchException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.TextEdit;

public class TokensMatcher {

  private final List<Integer> _numberPresentation;
  private final TokensIdentifier _identifier;
  private final Map<Integer, ASTNode> _nodeMap;
  private final NodeLocator _locator;
  private Modification _modification;
  private BuggyFile _buggyFile;
  private Set<String> _availableVars;

  private Map<String, Set<IMethodBinding>> _methodBindings;

  public TokensMatcher(TokensIdentifier identifier, NodeLocator locator, BuggyFile buggyFile,
      Map<String, Set<IMethodBinding>> methodBindings)
      throws BadLocationException {
    _identifier = identifier;
    _locator = locator;
    _numberPresentation = locator.getNumberPresentation();
    _nodeMap = locator.getMap();
    _buggyFile = buggyFile;
    _modification = new Modification(buggyFile);
    _availableVars =
        buggyFile.getAvailableVars(
            _buggyFile.getDocument().getLineOfOffset(_nodeMap.get(0).getStartPosition()));
    _methodBindings = methodBindings;
  }

  public int count(Integer obj, List<Integer> list) {
    int count = 0;
    for (Object o : list) {
      if (o.equals(obj)) {
        count++;
      }
    }
    return count;
  }

  public List<Patch> tryMakeFix(List<MatchedPattern> patterns) {

    List<Patch> patches = new LinkedList<>();
    //a list of map which contains the mapping from the token to the possible replacements
    List<Map<Integer, List<Pair<Integer, MatchedPattern>>>> replaceMap = new LinkedList<>();

    // Replacement
    Map<Integer, List<Pair<Integer, MatchedPattern>>> mutateMap = new HashMap<>();
    for (int i = 0; i < _numberPresentation.size(); i++) {
      List<AbstractToken> mutated = _identifier.getToken(_numberPresentation.get(i))
          .makeMutations();
      if (mutated != null) {
        List<Pair<Integer, MatchedPattern>> candidate = new ArrayList<>();
        for (AbstractToken token : mutated) {
          candidate.add(new Pair(_identifier.getId(token), MatchedPattern.mutatePattern));
          mutateMap.put(i, candidate);
        }
      }
    }

    replaceMap.add(mutateMap);

    for (MatchedPattern candidate : patterns) {
      Map<Integer, List<Pair<Integer, MatchedPattern>>> result = tryMakeReplacement(candidate);
      if (!result.isEmpty()) {
        replaceMap.add(result);
      }
    }

    replaceLoop:
    for (Map<Integer, List<Pair<Integer, MatchedPattern>>> x : replaceMap) {
      //List<Integer> is numberPresentation after replacement, List<MatchedPattern> is the list of patterns used
      Set<Pair<List<Integer>, List<MatchedPattern>>> replaceResult = new HashSet<>();
      replaceResult.add(new Pair(_numberPresentation, new ArrayList<>()));

      for (Entry<Integer, List<Pair<Integer, MatchedPattern>>> entry : x.entrySet()) {
        for (Pair<Integer, MatchedPattern> now : entry.getValue()) {
          Set<Pair<List<Integer>, List<MatchedPattern>>> tempp = new HashSet<>();
          for (Pair<List<Integer>, List<MatchedPattern>> x1 : replaceResult) {
            List<Integer> temp = new ArrayList(x1.getFirst());
            temp.set(entry.getKey(), now.getFirst());
            List<MatchedPattern> pattUsed = new ArrayList(x1.getSecond());
            pattUsed.add(now.getSecond());
            tempp.add(new Pair<>(temp, pattUsed));
          }
          replaceResult.addAll(tempp);
        }
        if (replaceResult.size() > 100000) {
          break;
        }
      }

      replaceResult.remove(_numberPresentation);

      // List<Integer> is the number presentation after replacement, Integer is the frequency of the patch
      for (Pair<List<Integer>, List<MatchedPattern>> listOfCandidates : replaceResult) {

        if (listOfCandidates.getSecond().isEmpty()) {
          continue;
        }

        try {
          double minPriority = 10;

          //Integer is the index of the bugToken in the number presentation,
          //ASTNode is the ASTNode of the bugToken, AbstractToken is the patch Token.
          List<Pair<Integer, Pair<ASTNode, AbstractToken>>> eventRecords = new ArrayList<>();

          out:
          for (int index = 0; index < _numberPresentation.size(); index++) {
            if (!listOfCandidates.getFirst().get(index).equals(_numberPresentation.get(index))) {

              ASTNode originNode = _locator.getMap().get(index);
              StructuralPropertyDescriptor property = originNode.getLocationInParent();

              eventRecords.add(new Pair<>(index, new Pair<>(originNode,
                  _identifier.getToken(listOfCandidates.getFirst().get(index)))));

              //get the element Type of the original node
              Class elementClass;
              if (property.isChildListProperty()) {
                elementClass = ((ChildListPropertyDescriptor) property).getElementType();
              } else {
                elementClass = ((ChildPropertyDescriptor) property).getChildType();
              }

              //send the entire expression to Method buildNode to build the correct kind of ASTNode.
              while (elementClass != Expression.class
                  && elementClass != VariableDeclarationFragment.class
                  && elementClass != Type.class) {
                originNode = originNode.getParent();
                if (originNode instanceof Statement) {
                  continue out;
                }
                property = originNode.getLocationInParent();
                if (property.isChildListProperty()) {
                  elementClass = ((ChildListPropertyDescriptor) property).getElementType();
                } else {
                  elementClass = ((ChildPropertyDescriptor) property).getChildType();
                }
              }

              Pair<ASTNode, Double> target =
                  _identifier
                      .getToken(listOfCandidates.getFirst().get(index))
                      .buildNode(originNode, _buggyFile, _availableVars);

              if (target == null) {
                continue;
              }

              if (target.getFirst() instanceof MethodInvocation) {
                if (!Purifier.isValid(originNode, (MethodInvocation) target.getFirst(),
                    _methodBindings.get(
                        ((MethodInvocation) target.getFirst()).getName().toString()))) {
                  continue;
                }
              }

              _modification.replace(originNode, target.getFirst());

              minPriority = Math.min(minPriority, target.getSecond());
            }
          }

          TextEdit patch = _modification.buildPatch();

          patches.add(new Patch(patch, ModificationEvent.recordReplacementList(eventRecords),
              listOfCandidates.getSecond(), minPriority, _buggyFile,
              PatchType.token));

          if (patches.size() >= 30000) {
            break replaceLoop;
          }

        } catch (IllegalArgumentException e) {
          // Unavailable
        } catch (WrongPatchException e) {
          // Unavailable
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    // Insertion
    for (MatchedPattern candidate : patterns) {
      for (Traid<TextEdit, MatchedPattern, ModificationEvent> patch : tryMakeInsertion(candidate)) {
        List<MatchedPattern> patternList = new ArrayList<>();
        patternList.add(patch.getSecond());
        patches.add(
            new Patch(patch.getFirst(), new EventList(patch.getThird()),
                patternList, 1,
                _buggyFile, PatchType.token));
      }
    }

    return patches;
  }

  /**
   * Map<toReplaceTokenNo,List<Pair<patchTokenID,patchFrequency>>>
   */

  public Map<Integer, List<Pair<Integer, MatchedPattern>>> tryMakeReplacement(
      MatchedPattern inputPattern) {
    Map<Integer, List<Pair<Integer, MatchedPattern>>> result = new HashMap<>();
    List<Integer> pattern = inputPattern.getPatterns();
    int[] match = inputPattern.getMatch();
    for (int indexOfCurrentPatchToken = 0;
        indexOfCurrentPatchToken < match.length;
        indexOfCurrentPatchToken++) {
      if (match[indexOfCurrentPatchToken] == -1) {
        int prev = indexOfCurrentPatchToken, rear = indexOfCurrentPatchToken;
        while (prev != 0 && match[--prev] == -1)
          ;
        while (rear != match.length - 1 && match[++rear] == -1)
          ;
        int stopLocation;
        if (rear == match.length - 1 && match[rear] == -1) {
          stopLocation = _numberPresentation.size();
        } else {
          stopLocation = match[rear];
        }
        for (int toReplaceTokenIndex = match[prev] + 1;
            toReplaceTokenIndex < stopLocation;
            toReplaceTokenIndex++) {
          //We dont replace null literal?
          if (_identifier.getToken(
              _numberPresentation.get(toReplaceTokenIndex)) instanceof NullLiteralToken) {
            continue;
          }
          if (_identifier
              .getToken(pattern.get(indexOfCurrentPatchToken))
              .isCompatibleWith(
                  _identifier.getToken(_numberPresentation.get(toReplaceTokenIndex)),
                  _availableVars)) {
            if (result.containsKey(toReplaceTokenIndex)) {
              result
                  .get(toReplaceTokenIndex)
                  .add(
                      new Pair(pattern.get(indexOfCurrentPatchToken), inputPattern));
            } else {
              List<Pair<Integer, MatchedPattern>> candidatePatterns = new ArrayList<>();
              candidatePatterns.add(
                  new Pair(pattern.get(indexOfCurrentPatchToken), inputPattern));
              result.put(toReplaceTokenIndex, candidatePatterns);
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Traid <A, B, C> means that A is the TextEdit, B is the Frequency, C is the ModificationEvent.
   */
  public List<Traid<TextEdit, MatchedPattern, ModificationEvent>> tryMakeInsertion(
      MatchedPattern inputPattern) {
    List<Traid<TextEdit, MatchedPattern, ModificationEvent>> patches = new LinkedList<>();
    int[] match = inputPattern.getMatch();
    // start with the second element
    int start, end = 0;
    List<Pair<Integer, Integer>> insertSection = new ArrayList<>();
    while (true) {
      start = end;
      while (start < match.length - 1 && match[start] != -1) {
        start++;
      }
      if (start == match.length - 1) {
        insertSection.add(new Pair<>(start, start));
        break;
      }
      end = start;
      while (end < match.length - 1 && match[end] == -1) {
        end++;
      }
      if (end == match.length - 1) {
        insertSection.add(new Pair<>(start, end));
        break;
      }
      insertSection.add(new Pair<>(start, end - 1));
    }
    for (Pair<Integer, Integer> section : insertSection) {
      if (section.getFirst() == 0) {
        continue;
      }
      Integer previousIndex = match[section.getFirst() - 1];
      Pair<TextEdit, ModificationEvent> patch =
          tryInsertAfter(
              _nodeMap.get(previousIndex),
              inputPattern.getPatterns().subList(section.getFirst(), section.getSecond() + 1));
      if (patch != null) {
        patches.add(new Traid(patch.getFirst(), inputPattern, patch.getSecond()));
      }
    }
    return patches;
  }

  /**
   * Pair<A,B> means that A is the textEdit of the patch and B is the ModificationEvent.
   */
  @SuppressWarnings("unchecked")
  private Pair tryInsertAfter(ASTNode oriNode, List<Integer> candidates) {
    List<AbstractToken> tokensToInsert = new ArrayList<>();
    if (oriNode.getParent() == null) {
      return null;
    }
/*
    if(oriNode.getParent() instanceof MethodInvocation){
      MethodInvocation invocation = (MethodInvocation) oriNode.getParent();
      Integer maxCount = _methodMaxParams.get(invocation.getName());
      if(maxCount != null && invocation.arguments().size()>=maxCount){
        return null;
      }
    }
*/
    if (oriNode.getLocationInParent().getId().equals("arguments")) {
      ASTNode node = ASTNode.copySubtree(oriNode.getAST(), oriNode.getParent());
      List<ASTNode> nodesToInsert = new ArrayList<>();
      for (Integer candidate : candidates) {
        AbstractToken token = _identifier.getToken(candidate);
        if (token.isLeafNode()) {
          try {
            tokensToInsert.add(token);
            nodesToInsert.add(
                ASTNode.copySubtree(
                    oriNode.getAST(),
                    token.buildNode(oriNode, _buggyFile, _availableVars).getFirst()));
          } catch (Exception e) {
          }
        }
      }
      if (node instanceof MethodInvocation) {
        MethodInvocation methodInvocation = (MethodInvocation) node;
        methodInvocation
            .arguments()
            .addAll(
                ((MethodInvocation) (oriNode.getParent())).arguments().indexOf(oriNode) + 1,
                nodesToInsert);
        if (!Purifier.isValid((MethodInvocation) oriNode.getParent(), methodInvocation,
            _methodBindings.get(methodInvocation.getName().toString()))) {
          return null;
        }
      } else if (node instanceof ClassInstanceCreation) {
        ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
        classInstanceCreation
            .arguments()
            .addAll(
                ((ClassInstanceCreation) (oriNode.getParent())).arguments().indexOf(oriNode) + 1,
                nodesToInsert);
      }
      _modification.replace(oriNode.getParent(), node);
      return new Pair(_modification.buildPatch(), new ParamInsertEvent(tokensToInsert, 0));
    } else if (oriNode.getLocationInParent().getId().equals("name")) {
      ASTNode node = ASTNode.copySubtree(oriNode.getAST(), oriNode.getParent());
      List<ASTNode> nodesToInsert = new ArrayList<>();
      for (Integer candidate : candidates) {
        AbstractToken token = _identifier.getToken(candidate);
        if (token.isLeafNode()) {
          try {
            tokensToInsert.add(token);
            nodesToInsert.add(
                ASTNode.copySubtree(
                    oriNode.getAST(),
                    token.buildNode(oriNode, _buggyFile, _availableVars).getFirst()));
          } catch (Exception e) {
            //System.out.println(e);
          }
        }
      }
      if (node instanceof MethodInvocation) {
        MethodInvocation methodInvocation = (MethodInvocation) node;
        methodInvocation.arguments().addAll(0, nodesToInsert);
        if (!Purifier.isValid((MethodInvocation) oriNode.getParent(), methodInvocation,
            _methodBindings.get(methodInvocation.getName().toString()))) {
          return null;
        }
      } else if (node instanceof ClassInstanceCreation) {
        ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;
        classInstanceCreation.arguments().addAll(0, nodesToInsert);
      }
      _modification.replace(oriNode.getParent(), node);
      return new Pair(_modification.buildPatch(), new ParamInsertEvent(tokensToInsert, 0));
    }
    return null;
  }
}
