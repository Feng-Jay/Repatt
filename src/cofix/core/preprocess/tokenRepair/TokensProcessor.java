package cofix.core.preprocess.tokenRepair;

import cofix.common.config.Constant;
import cofix.common.run.Executor;
import cofix.common.util.JavaFile;
import cofix.common.util.ListTool;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.Divider;
import cofix.core.modification.BuggyFile;
import cofix.core.pattern.MatchedPattern;
import cofix.core.preprocess.Builder;
import cofix.core.preprocess.Patch;
import cofix.core.preprocess.Patch.PatchType;
import cofix.core.preprocess.Patch.ValidateStatus;
import cofix.core.preprocess.Tree;
import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.BooleanLiteralToken;
import cofix.core.preprocess.token.CharaterLiteralToken;
import cofix.core.preprocess.token.IdentifierToken;
import cofix.core.preprocess.token.InfixExpressionOperatorToken;
import cofix.core.preprocess.token.NullLiteralToken;
import cofix.core.preprocess.token.NumberLiteralToken;
import cofix.core.preprocess.token.PostfixExpressionOperatorToken;
import cofix.core.preprocess.token.PrefixExpressionOperatorToken;
import cofix.core.preprocess.token.PrimitiveTypeToken;
import cofix.core.preprocess.token.QualifiedNameToken;
import cofix.core.preprocess.token.SimpleNameToken;
import cofix.core.preprocess.token.SimpleNameToken.Usage;
import cofix.core.preprocess.token.SimpleTypeToken;
import cofix.core.preprocess.token.StringLiteralToken;
import cofix.core.preprocess.token.ThisExpressionToken;
import cofix.core.preprocess.token.TypeLiteralToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.BadLocationException;

public class TokensProcessor {

    private final TokensIdentifier _tokensIdentifier = new TokensIdentifier();
    private final Tree tree = new Tree();
    private final MethodDeclarationVisitor _methodVisitor = new MethodDeclarationVisitor();
    private final Map<String, Integer> _methodMaxParameters = new HashMap<>();

    private final Map<String, Set<IMethodBinding>> _methodBindings = new HashMap<>();

    List<List<Integer>> _allSequences = new LinkedList<>();
    private Logger _logger = Logger.getLogger(TokensProcessor.class.getName());
    private BuggyFile buggyFile;

    private List<Patch> sortedPatches;
    private List<Pair<BuggyFile, Integer>> similarLocations = new ArrayList<>();

    public static int validatePatches(Subject subject, BuggyFile buggyFile, List<Patch> sortedPatches,
                                      Pair<String, Integer> currLoc,
                                      boolean useSystemEdit, List<Pair<BuggyFile, Integer>> locations) {
        if (sortedPatches == null) {
            return 0;
        }

        Logger logger = Logger.getLogger(TokensProcessor.class.getName());
        logger.info("Start to validate token level patches");
        Executor.execute(new String[]{"/bin/bash", "-c",
                Constant.COMMAND_CD + subject.getHome() + " && " + "git checkout -f HEAD "
                        + subject.getHome() + subject.getSsrc()});
        int testCount = 0;
        int sumTest = 0;
        int successCount = 0;
        for (Patch patch : sortedPatches) {
            if (sumTest++ >= Constant.ALL_TOKEN_MAX_PATCH_ONE_LOCATION) {
                logger.info("Too much uncompilable patches, stop to validate");
                break;
            }
/*
      if(testCount >= Constant.TOKEN_TEST_NUM) {
        logger.info("Token level patch test finished");
        break;
      }
*/
            if (successCount >= Constant.TOKEN_MAX_PATCH_ONE_LOCATION) {
                logger.info("patches are already successful in this location");
                return Constant.TOKEN_MAX_PATCH_ONE_LOCATION;
            }
            try {
                logger.info("No. " + sumTest + " Token Testing:" + patch.getFixedString() + " using block "
                        + patch.getBlockId() + " with similarity "
                        + patch.getSimilarity());
                logger.info("Pattern : " + patch.printPattern(subject));
                ValidateStatus status = patch.validate();
                switch (status) {
                    case SUCCESS:
                        successCount++;
                        testCount++;
                        logger.info("===============================");
                        logger.info("TEST SUCCESSFUL with " + patch.getPatchType().toString());
                        logger.info("Patch: " + patch.getFixedString());
                        logger.info("at Location => " + currLoc.getFirst() + ":" + currLoc.getSecond());
                        logger.info("Diff File Path:" + patch.getBuggyFile().getFilePath());
                        logger.info("Pattern occurrence: " + patch.getOccurrences());
                        logger.info("Pattern:" + patch.printPattern(subject));
                        logger.info("===============================");
                        if (useSystemEdit) {
                            int result = patch.trySystemEdit(locations);
                            if (result > 0) {
                                logger.info("SUCCESSFUL with advanced SystemEdit");
                                successCount += result;
                            }
                        }
                        break;
                    case TEST_FAILED:
                        logger.info("Token TEST FAILED");
                        testCount++;
                        if (useSystemEdit) {
                            int result = patch.trySystemEdit(locations);
                            if (result > 0) {
                                logger.info("SUCCESSFUL with SystemEdit");
                                successCount += result;
                            }
                        }
                        break;
                    case COMPILE_FAILED:
                        System.out.println("Token COMPILE FAILED");
                        break;
                }
            } catch (BadLocationException e) {
                buggyFile.restoreFile();
            } catch (Exception e) {
            } finally {
                Executor.execute(new String[]{"/bin/bash", "-c",
                        Constant.COMMAND_CD + subject.getHome() + " && " + "git checkout -f HEAD "
                                + subject.getHome() + subject.getSsrc()});
                buggyFile.reloadFile();
            }
        }

        buggyFile.restoreFile();
        return successCount;
    }


    public Integer getId(String tokenName) {
        AbstractToken token = new IdentifierToken(tokenName);
        return _tokensIdentifier.getId(token);
    }

    public void collectCu(CompilationUnit cu, String path) {
        _methodVisitor.setPath(path, cu);
        cu.accept(_methodVisitor);
    }

    public TokensIdentifier getTokensIdentifier() {
        return _tokensIdentifier;
    }

    public List<Integer> processNode(ASTNode node) {
        return _methodVisitor.processNode(node);
    }

    public BuggyFile getBuggyFile() {
        return buggyFile;
    }


    public List<Patch> getSortedPatches() {
        return sortedPatches;
    }

    public List<Pair<BuggyFile, Integer>> getSimilarLocations() {
        return similarLocations;
    }

    public void doFix(Subject subject, Pair<String, Integer> loc, Divider systemDivider) {
        if (tree.isEmpty()) {
            doMining();
        }

        _logger.info("Trying token-level fix on " + loc.getFirst() + ":" + loc.getSecond());

        buggyFile = BuggyFile.getBuggyFile(subject, loc);

        ASTNode bugNode = buggyFile.getStatement(loc.getSecond());

        if (bugNode == null) {
            return;
        }

        sortedPatches = doSingleFix(bugNode, buggyFile, false, loc);
        

        similarLocations = new ArrayList<>();

        for (Pair<String, Integer> similar : systemDivider.getSimilarLocations(subject, loc)) {
            similarLocations.add(
                    new Pair<>(BuggyFile.getBuggyFile(subject, similar), similar.getSecond()));
        }

    }

    public List<Patch> doSingleFix(ASTNode bugNode, BuggyFile buggyFile, boolean isCombination,
                                   Pair<String, Integer> loc) {
        NodeLocator locator = new NodeLocator(_tokensIdentifier);

        bugNode.accept(locator);

        List<Integer> bugNumPresentation = locator.getNumberPresentation();

        List<MatchedPattern> patterns = tree.do_dfs(bugNumPresentation);

        TokensMatcher matcher = null;
        try {
            matcher = new TokensMatcher(_tokensIdentifier, locator, buggyFile, _methodBindings);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        List<Patch> oriPatches;

        oriPatches = matcher.tryMakeFix(patterns);

        for (Patch patch : oriPatches) {
            patch.setPatchType(isCombination ? PatchType.combination : PatchType.token);
        }

        Set<Patch> patches = new HashSet<>();

        oriPatches.forEach(
                p -> {
                    if (p.getOccurrences() != Integer.MAX_VALUE) {
                        patches.add(p);
                    }
                });

        List<Patch> sortedPatches = new LinkedList<>(patches);

        if (sortedPatches.isEmpty()) {
            return sortedPatches;
        }

        int maxOccurrences =
                sortedPatches.stream()
                        .max(Comparator.comparing(Patch::getOccurrences))
                        .get()
                        .getOccurrences();

        sortedPatches.forEach(p -> p.doNormalization(maxOccurrences));
        sortedPatches.sort(Comparator.comparing(Patch::getPossibility).reversed());
        if (Constant.USE_FILTER) {
            PatchFilter filter = new PatchFilter(bugNode);
            try {
                filter.filt(sortedPatches, _tokensIdentifier, buggyFile.getSubject(),
                        loc.getFirst() + loc.getSecond().toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return sortedPatches;
    }

    public void doMining() {
        final Set<String> filters = new HashSet<>(Arrays.asList("(", ")", "{", "}", ",", "\"", "."));
        Builder builder = new Builder();
        for (List<Integer> oriList : _allSequences) {
            List<Integer> list =
                    oriList.stream()
                            .filter(t -> ((!filters.contains(_tokensIdentifier.getToken(t).getName()))
                            ))
                            .collect(Collectors.toList());
            for (List<Integer> splited :
                    ListTool.splitStream(
                            list, _tokensIdentifier.getId(new AbstractToken(";", ";", null) {
                            }))) {
                List<Queue<Integer>> result = new LinkedList<>();
                builder.buildSeqs(result, splited, 2);
                Tree mirror = new Tree();
                result.forEach(q -> tree.build(q, mirror));
            }
        }
        System.out.println("Tree has been built!");
    }

    public class MethodDeclarationVisitor extends ASTVisitor {

        TokenVisitor _tokenVisitor = new TokenVisitor();

        private String _path;
        private CompilationUnit _cu;


        public void setPath(String path, CompilationUnit cu) {
            _path = path;
            _cu = cu;
            _tokenVisitor.setPath(path, cu);
        }

        public boolean visit(MethodDeclaration node) {
            IMethodBinding methodBinding = node.resolveBinding();
            String name = node.getName().toString();
            if (methodBinding != null) {
                if (_methodBindings.containsKey(name)) {
                    _methodBindings.get(name).add(methodBinding);
                } else {
                    _methodBindings.put(name, new HashSet<>());
                    _methodBindings.get(name).add(methodBinding);
                }
            }
            if (_methodMaxParameters.containsKey(node.getName().toString())) {
                Integer maxParameters = _methodMaxParameters.get(node.getName().toString());
                if (node.parameters().size() > maxParameters) {
                    _methodMaxParameters.put(node.getName().toString(), node.parameters().size());
                }
            } else {
                _methodMaxParameters.put(node.getName().toString(), node.parameters().size());
            }
            node.accept(_tokenVisitor);
            return false;
        }

        public List<Integer> processNode(ASTNode node) {
            return _tokenVisitor.process(node);
        }

        public class TokenVisitor extends ASTVisitor {

            private String _path;
            private CompilationUnit _cu;

            public void setPath(String path, CompilationUnit cu) {
                _path = path;
                _cu = cu;
            }

            public boolean visit(IfStatement node) {
                _allSequences.add(process(node));
                return false;
            }

            public boolean visit(ReturnStatement node) {
                _allSequences.add(process(node));
                return false;
            }

            public boolean visit(ForStatement node) {
                _allSequences.add(process(node));
                return false;
            }

            public boolean visit(ExpressionStatement node) {
                _allSequences.add(process(node));
                return false;
            }

            public boolean visit(EnhancedForStatement node) {
                _allSequences.add(process(node));
                return false;
            }

            public boolean visit(DoStatement node) {
                _allSequences.add(process(node));
                return false;
            }

            public boolean visit(WhileStatement node) {
                _allSequences.add(process(node));
                return false;
            }

            public boolean visit(VariableDeclarationStatement node) {
                _allSequences.add(process(node));
                return false;
            }

            public List<Integer> process(ASTNode node) {
                if (node instanceof InfixExpression) {
                    return process((InfixExpression) node);
                } else if (node instanceof NumberLiteral) {
                    return process((NumberLiteral) node);
                } else if (node instanceof SimpleName) {
                    return process((SimpleName) node);
                } else if (node instanceof Assignment) {
                    return process((Assignment) node);
                } else if (node instanceof ArrayAccess) {
                    return process((ArrayAccess) node);
                } else if (node instanceof BooleanLiteral) {
                    return process((BooleanLiteral) node);
                } else if (node instanceof CharacterLiteral) {
                    return process((CharacterLiteral) node);
                } else if (node instanceof ConditionalExpression) {
                    return process((ConditionalExpression) node);
                } else if (node instanceof FieldAccess) {
                    return process((FieldAccess) node);
                } else if (node instanceof LambdaExpression) {
                    return process((LambdaExpression) node);
                } else if (node instanceof MethodInvocation) {
                    return process((MethodInvocation) node);
                } else if (node instanceof QualifiedName) {
                    return process((QualifiedName) node);
                } else if (node instanceof NullLiteral) {
                    return process((NullLiteral) node);
                } else if (node instanceof ParenthesizedExpression) {
                    return process((ParenthesizedExpression) node);
                } else if (node instanceof CastExpression) {
                    return process((CastExpression) node);
                } else if (node instanceof PostfixExpression) {
                    return process((PostfixExpression) node);
                } else if (node instanceof PrefixExpression) {
                    return process((PrefixExpression) node);
                } else if (node instanceof StringLiteral) {
                    return process((StringLiteral) node);
                } else if (node instanceof SuperFieldAccess) {
                    return process((SuperFieldAccess) node);
                } else if (node instanceof TypeLiteral) {
                    return process((TypeLiteral) node);
                } else if (node instanceof SuperMethodInvocation) {
                    return process((SuperMethodInvocation) node);
                } else if (node instanceof ThisExpression) {
                    return process((ThisExpression) node);
                } else if (node instanceof ClassInstanceCreation) {
                    return process((ClassInstanceCreation) node);
                } else if (node instanceof IfStatement) {
                    return process((IfStatement) node);
                } else if (node instanceof ForStatement) {
                    return process((ForStatement) node);
                } else if (node instanceof Block) {
                    return process((Block) node);
                } else if (node instanceof ExpressionStatement) {
                    return process((ExpressionStatement) node);
                } else if (node instanceof BreakStatement) {
                    return process((BreakStatement) node);
                } else if (node instanceof ContinueStatement) {
                    return process((ContinueStatement) node);
                } else if (node instanceof DoStatement) {
                    return process((DoStatement) node);
                } else if (node instanceof EnhancedForStatement) {
                    return process((EnhancedForStatement) node);
                } else if (node instanceof ReturnStatement) {
                    return process((ReturnStatement) node);
                } else if (node instanceof ThrowStatement) {
                    return process((ThrowStatement) node);
                } else if (node instanceof LabeledStatement) {
                    return process((LabeledStatement) node);
                } else if (node instanceof WhileStatement) {
                    return process((WhileStatement) node);
                } else if (node instanceof ArrayCreation) {
                    return process((ArrayCreation) node);
                } else if (node instanceof ArrayInitializer) {
                    return process((ArrayInitializer) node);
                } else if (node instanceof InstanceofExpression) {
                    return process((InstanceofExpression) node);
                } else if (node instanceof MethodReference) {
                    return process((MethodReference) node);
                } else if (node instanceof VariableDeclarationFragment) {
                    return process((VariableDeclarationFragment) node);
                } else if (node instanceof VariableDeclarationStatement) {
                    return process((VariableDeclarationStatement) node);
                } else if (node instanceof SwitchStatement) {
                    return process((SwitchStatement) node);
                } else if (node instanceof VariableDeclarationExpression) {
                    return process((VariableDeclarationExpression) node);
                }

                // TODO:ADD MORE PROCESS FUNCTION!!
                List<Integer> ans = new ArrayList<>(1);
                if (node == null) {
                    ans.add(getId(""));
                    return ans;
                }
                ans.add(getId(node.toString()));
                return ans;
            }

            public List<Integer> process(ExpressionStatement exprStmt) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(exprStmt.getExpression()));
                ans.add(getId(";"));
                return ans;
            }

            public List<Integer> process(VariableDeclarationExpression varDeclExpr) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(varDeclExpr.getType()));
                ans.add(getId(" "));
                for (Object item : varDeclExpr.fragments()) {
                    ans.addAll(process((VariableDeclarationFragment) item));
                }
                return ans;
            }

            public List<Integer> process(SwitchStatement switchStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("switch"));
                ans.add(getId("("));
                ans.addAll(process(switchStatement.getExpression()));
                ans.add(getId(")"));
                for (Object stmt : switchStatement.statements()) {
                    ans.addAll(process((ASTNode) stmt));
                }
                return ans;
            }

            public List<Integer> process(SwitchCase switchCase) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("case"));
                ans.addAll(process(switchCase.getExpression()));
                ans.add(getId(":"));
                return ans;
            }

            public List<Integer> process(Assignment assign) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(assign.getLeftHandSide()));
                ans.add(getId(assign.getOperator().toString()));
                ans.addAll(process(assign.getRightHandSide()));
                return ans;
            }

            HashSet<String> set = new HashSet<>();

            public List<Integer> process(SimpleName simpleName) {
                List<Integer> ans = new ArrayList<>(1);
                int id = _tokensIdentifier.getId(new SimpleNameToken(simpleName, _path, _cu));
                StructuralPropertyDescriptor property = simpleName.getLocationInParent();
                Class _elementClass = null;
                if (property.isChildListProperty()) {
                    _elementClass = ((ChildListPropertyDescriptor) property).getElementType();
                } else if (property.isChildProperty()) {
                    _elementClass = ((ChildPropertyDescriptor) property).getChildType();
                }
                set.add(
                        simpleName.getLocationInParent().getId() + " " + simpleName.getLocationInParent()
                                .getNodeClass() + " " + _elementClass.toString() + "\n");
                _tokensIdentifier.addCount(id, simpleName);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(CastExpression castExpr) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("("));
                ans.add(getId(castExpr.getType().toString()));
                ans.add(getId(")"));
                ans.addAll(process(castExpr.getExpression()));
                return ans;
            }

            public List<Integer> process(ClassInstanceCreation classInstanceCreation) {
                List<Integer> ans = new LinkedList<>();
                Expression expression = classInstanceCreation.getExpression();
                if (expression != null) {
                    ans.addAll(process(expression));
                    ans.add(getId("."));
                }
                ans.add(getId("new"));
                // ?IS there should be a <TYPE>??
                ans.addAll(process(classInstanceCreation.getType()));
                List args = classInstanceCreation.arguments();
                ans.add(getId("("));
                if (args.size() != 0) {
                    for (Object arg : args) {
                        ans.addAll(process((Expression) arg));
                        ans.add(getId(","));
                    }
                    ans.remove(ans.size() - 1);
                }
                ans.add(getId(")"));
                ASTNode anonymousClassDecl = classInstanceCreation.getAnonymousClassDeclaration();
                if (anonymousClassDecl != null) {
                    ans.addAll(process(anonymousClassDecl));
                }
                return ans;
            }

            public List<Integer> process(NumberLiteral numLit) {
                List<Integer> ans = new ArrayList<>(1);
                int id = _tokensIdentifier.getId(new NumberLiteralToken(numLit));
                _tokensIdentifier.addCount(id, numLit);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(InfixExpression infixExpr) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(infixExpr.getLeftOperand()));
                int id = _tokensIdentifier.getId(new InfixExpressionOperatorToken(infixExpr));
                _tokensIdentifier.addCount(id, infixExpr);
                ans.add(id);
                ans.addAll(process(infixExpr.getRightOperand()));
                return ans;
            }

            public List<Integer> process(ArrayAccess arrayAccess) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(arrayAccess.getArray()));
                ans.add(getId("["));
                ans.addAll(process(arrayAccess.getIndex()));
                ans.add(getId("]"));
                return ans;
            }

            public List<Integer> process(BooleanLiteral booleanLiteral) {
                List<Integer> ans = new ArrayList<>(1);
                int id = _tokensIdentifier.getId(new BooleanLiteralToken(booleanLiteral));
                _tokensIdentifier.addCount(id, booleanLiteral);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(CharacterLiteral characterLiteral) {
                List<Integer> ans = new ArrayList<>(1);
                int id = _tokensIdentifier.getId(new CharaterLiteralToken(characterLiteral));
                _tokensIdentifier.addCount(id, characterLiteral);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(ConditionalExpression conditionalExpression) {
                // max=(a>b)?a:b;
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("("));
                ans.addAll(process(conditionalExpression.getExpression()));
                ans.add(getId(")"));
                ans.add(getId("?"));
                ans.addAll(process(conditionalExpression.getThenExpression()));
                ans.add(getId(":"));
                ans.addAll(process(conditionalExpression.getElseExpression()));
                return ans;
            }

            public List<Integer> process(FieldAccess fieldAccess) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(fieldAccess.getExpression()));
                ans.add(getId("."));
                int id = _tokensIdentifier.getId(
                        new SimpleNameToken(fieldAccess.getName(), _path, _cu, Usage.Field));
                _tokensIdentifier.addCount(id, fieldAccess);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(LambdaExpression lambdaExpression) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("("));
                List para = lambdaExpression.parameters();
                if (para != null) {
                    for (Object parameter : para) {
                        ans.addAll(process((ASTNode) parameter));
                        ans.add(getId(","));
                    }
                    ans.remove(ans.size() - 1);
                }
                ans.add(getId(")"));
                ans.add(getId("->"));
                ans.addAll(process(lambdaExpression.getBody()));
                return ans;
            }

            public List<Integer> process(MethodInvocation methodInvocation) {
                IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
                String name = methodInvocation.getName().toString();
                if (methodBinding != null) {
                    if (_methodBindings.containsKey(name)) {
                        _methodBindings.get(name).add(methodBinding);
                    } else {
                        _methodBindings.put(name, new HashSet<>());
                        _methodBindings.get(name).add(methodBinding);
                    }
                }
                if (_methodMaxParameters.containsKey(methodInvocation.getName().toString())) {
                    Integer maxParameters = _methodMaxParameters.get(methodInvocation.getName().toString());
                    if (methodInvocation.arguments().size() > maxParameters) {
                        _methodMaxParameters.put(methodInvocation.getName().toString(),
                                methodInvocation.arguments().size());
                    }
                } else {
                    _methodMaxParameters.put(methodInvocation.getName().toString(),
                            methodInvocation.arguments().size());
                }
                List<Integer> ans = new LinkedList<>();
                Expression expression = methodInvocation.getExpression();
                if (expression != null) {
                    ans.addAll(process(expression));
                    ans.add(getId("."));
                }
                int id = _tokensIdentifier.getId(new SimpleNameToken(methodInvocation.getName(), _path, _cu,
                        Usage.Method));
                _tokensIdentifier.addCount(id, methodInvocation.getName());
                ans.add(id);
                List arguments = methodInvocation.arguments();
                if (arguments.size() != 0) {
                    for (Object arg : methodInvocation.arguments()) {
                        ans.addAll(process((Expression) arg));
                        ans.add(getId(","));
                    }
                    ans.remove(ans.size() - 1);
                }
                return ans;
            }

            public List<Integer> process(QualifiedName qualifiedName) {
                List<Integer> ans = new ArrayList<>(1);
                int id = _tokensIdentifier.getId(new QualifiedNameToken(qualifiedName));
                _tokensIdentifier.addCount(id, qualifiedName);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(NullLiteral nullLiteral) {
                List<Integer> ans = new ArrayList<>(1);
                int id = _tokensIdentifier.getId(new NullLiteralToken(nullLiteral));
                _tokensIdentifier.addCount(id, nullLiteral);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(ParenthesizedExpression parenthesizedExpression) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("("));
                ans.addAll(process(parenthesizedExpression.getExpression()));
                ans.add(getId(")"));
                return ans;
            }

            public List<Integer> process(PostfixExpression postfixExpression) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(postfixExpression.getOperand()));
                int id = _tokensIdentifier.getId(new PostfixExpressionOperatorToken(postfixExpression));
                _tokensIdentifier.addCount(id, postfixExpression);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(PrefixExpression prefixExpression) {
                List<Integer> ans = new LinkedList<>();
                int id = _tokensIdentifier.getId(new PrefixExpressionOperatorToken(prefixExpression));
                _tokensIdentifier.addCount(id, prefixExpression);
                ans.add(id);
                ans.addAll(process(prefixExpression.getOperand()));
                return ans;
            }

            public List<Integer> process(StringLiteral strLiteral) {
                List<Integer> ans = new ArrayList<>(1);
                ans.add(getId("\""));
                int id = _tokensIdentifier.getId(new StringLiteralToken(strLiteral));
                _tokensIdentifier.addCount(id, strLiteral);
                ans.add(id);
                ans.add(getId("\""));
                return ans;
            }

            public List<Integer> process(SuperFieldAccess superFieldAccess) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("super"));
                ans.add(getId("."));
                ans.addAll(process(superFieldAccess.getName()));
                return ans;
            }

            public List<Integer> process(TypeLiteral typeLiteral) {
                List<Integer> ans = new ArrayList<>(2);
                int id = _tokensIdentifier.getId(new TypeLiteralToken(typeLiteral));
                _tokensIdentifier.addCount(id, typeLiteral);
                ans.add(id);
                ans.add(getId("."));
                ans.add(getId("class"));
                return ans;
            }

            public List<Integer> process(ThisExpression thisExpr) {
                List<Integer> ans = new ArrayList<>(1);
                int id = _tokensIdentifier.getId(new ThisExpressionToken(thisExpr));
                _tokensIdentifier.addCount(id, thisExpr);
                ans.add(id);
                return ans;
            }

            public List<Integer> process(SuperMethodInvocation superMethodInvocation) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("super"));
                ans.add(getId("."));
                ans.add(_tokensIdentifier.getId(new SimpleNameToken(superMethodInvocation.getName(), _path,
                        _cu, Usage.SuperMethod)));
                ans.add(getId("("));
                List arguments = superMethodInvocation.arguments();
                if (arguments.size() != 0) {
                    for (Object arg : superMethodInvocation.arguments()) {
                        ans.addAll(process((Expression) arg));
                        ans.add(getId(","));
                    }
                    ans.remove(ans.size() - 1);
                }
                ans.add(getId(")"));
                return ans;
            }

            public List<Integer> process(IfStatement ifStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("if"));
                ans.add(getId("("));
                ans.addAll(process(ifStatement.getExpression()));
                ans.add(getId(")"));
                ans.addAll(process(ifStatement.getThenStatement()));
                if (ifStatement.getElseStatement() != null) {
                    ans.add(getId("else"));
                    ans.addAll(process(ifStatement.getElseStatement()));
                }
                return ans;
            }

            public List<Integer> process(ForStatement forStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("for"));
                ans.add(getId("("));
                List initial = forStatement.initializers();
                if (initial.size() != 0) {
                    for (Object init : initial) {
                        ans.addAll(process((ASTNode) init));
                        ans.add(getId(","));
                    }
                }
                ans.remove(ans.size() - 1);
                ans.add(getId(";"));
                ans.addAll(process(forStatement.getExpression()));
                ans.add(getId(";"));
                List updater = forStatement.updaters();
                if (updater.size() != 0) {
                    for (Object update : updater) {
                        ans.addAll(process((ASTNode) update));
                        ans.add(getId(","));
                    }
                    ans.remove(ans.size() - 1);
                }
                ans.add(getId(")"));
                ans.addAll(process(forStatement.getBody()));
                return ans;
            }

            public List<Integer> process(EnhancedForStatement enhancedForStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("for"));
                ans.add(getId("("));
                ans.addAll(process(enhancedForStatement.getParameter()));
                ans.add(getId(":"));
                ans.addAll(process(enhancedForStatement.getExpression()));
                ans.add(getId(")"));
                ans.addAll(process(enhancedForStatement.getBody()));
                return ans;
            }

            public List<Integer> process(Block block) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("{"));
                List stmt = block.statements();
                for (Object st : stmt) {
                    ans.addAll(process((ASTNode) st));
                }
                ans.add(getId("}"));
                return ans;
            }

            public List<Integer> process(ArrayCreation arrayCreation) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("new"));
                List dimension = arrayCreation.dimensions();
                if (dimension.size() != 0) {
                    ans.add(getId(arrayCreation.getType().getElementType().toString()));
                    for (Object dim : dimension) {
                        ans.add(getId("["));
                        ans.addAll(process((Expression) dim));
                        ans.add(getId("]"));
                    }
                }
                ArrayInitializer initializer = arrayCreation.getInitializer();
                if (initializer != null) {
                    ans.addAll(process(arrayCreation.getInitializer()));
                }
                return ans;
            }

            public List<Integer> process(ArrayInitializer arrayInitial) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("{"));
                List args = arrayInitial.expressions();
                if (args.size() != 0) {
                    for (Object arg : args) {
                        ans.addAll(process((Expression) arg));
                        ans.add(getId(","));
                    }
                    ans.remove(ans.size() - 1);
                }
                ans.add(getId("}"));
                return ans;
            }

            public List<Integer> process(InstanceofExpression expr) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(expr.getLeftOperand()));
                ans.add(getId("instanceof"));
                ans.addAll(process(expr.getRightOperand()));
                return ans;
            }

            public List<Integer> process(BreakStatement breakStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("break"));
                //we dont process label
/*
        SimpleName label = breakStatement.getLabel();
        if (label != null) {
          ans.addAll(process(label));
        }
*/
                ans.add(getId(";"));
                return ans;
            }

            public List<Integer> process(ContinueStatement continueStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("continue"));
                SimpleName label = continueStatement.getLabel();
                if (label != null) {
                    ans.add(getId(label.toString()));
                }
                ans.add(getId(";"));
                return ans;
            }

            public List<Integer> process(ReturnStatement returnStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("return"));
                ans.addAll(process(returnStatement.getExpression()));
                ans.add(getId(";"));
                return ans;
            }

            public List<Integer> process(DoStatement doStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("do"));
                ans.addAll(process(doStatement.getBody()));
                ans.add(getId("while"));
                ans.add(getId("("));
                ans.addAll(process(doStatement.getExpression()));
                ans.add(getId(")"));
                return ans;
            }

            public List<Integer> process(ThrowStatement throwStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("throw"));
                ans.addAll(process(throwStatement.getExpression()));
                return ans;
            }

            public List<Integer> process(LabeledStatement labeledStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId(labeledStatement.getLabel().toString()));
                ans.add(getId(":"));
                ans.addAll(process(labeledStatement.getBody()));
                return ans;
            }

            public List<Integer> process(WhileStatement whileStatement) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId("while"));
                ans.add(getId("("));
                ans.addAll(process(whileStatement.getExpression()));
                ans.add(getId(")"));
                ans.addAll(process(whileStatement.getBody()));
                return ans;
            }

            public List<Integer> process(MethodReference methodRef) {
                List<Integer> ans = new LinkedList<>();
                if (methodRef instanceof CreationReference) {
                    ans.add(getId(((CreationReference) methodRef).getType().toString()));
                    ans.add(getId("::"));
                    ans.add(getId("new"));
                    return ans;
                } else if (methodRef instanceof ExpressionMethodReference) {
                    ans.addAll(process(((ExpressionMethodReference) methodRef).getExpression()));
                    ans.add(getId("::"));
                    ans.addAll(process(((ExpressionMethodReference) methodRef).getName()));
                    return ans;
                } else if (methodRef instanceof SuperMethodReference) {
                    Object qualifier = ((SuperMethodReference) methodRef).getQualifier();
                    if (qualifier != null) {
                        ans.addAll(process((ASTNode) qualifier));
                        ans.add(getId("."));
                    }
                    ans.add(getId("super"));
                    ans.add(getId("::"));
                    ans.addAll(process(((SuperMethodReference) methodRef).getName()));
                    return ans;
                } else {
                    ans.add(getId(((TypeMethodReference) methodRef).getType().toString()));
                    ans.add(getId("::"));
                    ans.addAll(process(((TypeMethodReference) methodRef).getName()));
                    return ans;
                }
            }

            public List<Integer> process(VariableDeclarationFragment variableDeclarationFragment) {
                List<Integer> ans = new LinkedList<>();
                ans.addAll(process(variableDeclarationFragment.getName()));
                if (variableDeclarationFragment.getInitializer() == null) {
                    return ans;
                }
                ans.add(getId("="));
                ans.addAll(process(variableDeclarationFragment.getInitializer()));
                return ans;
            }

            public List<Integer> process(Modifier modifier) {
                List<Integer> ans = new LinkedList<>();
                ans.add(getId(modifier.getKeyword().toString()));
                return ans;
            }

            public List<Integer> process(VariableDeclarationStatement variableDeclarationStmt) {
                List<Integer> ans = new LinkedList<>();
                List modifiers = variableDeclarationStmt.modifiers();
                for (Object modifier : modifiers) {
                    if (modifier instanceof SingleMemberAnnotation) {
                        continue;
                    }
                    ans.addAll(process((Modifier) modifier));
                }
                Type type = variableDeclarationStmt.getType();
                if (type instanceof SimpleType) {
                    int id = _tokensIdentifier.getId(new SimpleTypeToken((SimpleType) type));
                    ans.add(id);
                    _tokensIdentifier.addCount(id, type);
                } else if (type instanceof PrimitiveType) {
                    int id = _tokensIdentifier.getId(new PrimitiveTypeToken((PrimitiveType) type));
                    ans.add(id);
                    _tokensIdentifier.addCount(id, type);
                }
                List fragments = variableDeclarationStmt.fragments();
                if (fragments.size() != 0) {
                    for (Object fragment : fragments) {
                        ans.addAll(process((VariableDeclarationFragment) fragment));
                        ans.add(getId(","));
                    }
                    ans.remove(ans.size() - 1);
                }
                ans.add(getId(";"));
                return ans;
            }
        }
    }
}
