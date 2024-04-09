package cofix.core.preprocess.tokenRepair;

import cofix.core.preprocess.token.AbstractToken;
import cofix.core.preprocess.token.ArrayTypeToken;
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
import cofix.core.preprocess.token.SimpleTypeToken;
import cofix.core.preprocess.token.StringLiteralToken;
import cofix.core.preprocess.token.ThisExpressionToken;
import cofix.core.preprocess.token.TypeLiteralToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class NodeLocator extends ASTVisitor {

    private final Map<Integer, ASTNode> _nodeMap = new HashMap<>();
    private int index = 0;
    private final List<Integer> _numberPresentation = new ArrayList<>();
    private final TokensIdentifier _identifier;

    private Logger _logger = Logger.getLogger(NodeLocator.class);

    public List<Integer> getNumberPresentation() {
        return _numberPresentation;
    }

    private AbstractToken buildToken(ASTNode node) {
        if (node instanceof SimpleName) {
            return new SimpleNameToken((SimpleName) node);
        } else if (node instanceof QualifiedName) {
            return new QualifiedNameToken((QualifiedName) node);
        } else if (node instanceof NumberLiteral) {
            return new NumberLiteralToken((NumberLiteral) node);
        } else if (node instanceof StringLiteral) {
            return new StringLiteralToken((StringLiteral) node);
        } else if (node instanceof InfixExpression) {
            return new InfixExpressionOperatorToken((InfixExpression) node);
        } else if (node instanceof PostfixExpression) {
            return new PostfixExpressionOperatorToken((PostfixExpression) node);
        } else if (node instanceof PrefixExpression) {
            return new PrefixExpressionOperatorToken((PrefixExpression) node);
        } else if (node instanceof BooleanLiteral) {
            return new BooleanLiteralToken((BooleanLiteral) node);
        } else if (node instanceof NullLiteral) {
            return new NullLiteralToken((NullLiteral) node);
        } else if (node instanceof CharacterLiteral) {
            return new CharaterLiteralToken((CharacterLiteral) node);
        } else if (node instanceof SimpleType) {
            return new SimpleTypeToken((SimpleType) node);
        } else if (node instanceof ThisExpression) {
            return new ThisExpressionToken((ThisExpression) node);
        } else if (node instanceof ArrayType) {
            return new ArrayTypeToken((ArrayType) node);
        } else if (node instanceof TypeLiteral) {
            return new TypeLiteralToken((TypeLiteral) node);
        } else if (node instanceof PrimitiveType) {
            return new PrimitiveTypeToken((PrimitiveType) node);
        } else if (node instanceof ParameterizedType) {
            return new SimpleTypeToken((SimpleType) ((ParameterizedType) node).getType());
        }
        _logger.error("Unsupported node type: " + node.toString());
        throw new IllegalArgumentException();
    }

    public NodeLocator(TokensIdentifier identifier) {
        _identifier = identifier;
    }

    private void mapLeafNode(ASTNode node) {
        if (node == null) {
            return;
        }
        _nodeMap.put(index++, node);
        _numberPresentation.add(_identifier.getId(buildToken(node)));
    }

    public void map(ASTNode node) {
        if (node == null) {
            return;
        }
        if (node instanceof ArrayAccess) {
            visit((ArrayAccess) node);
        } else if (node instanceof Assignment) {
            visit((Assignment) node);
        } else if (node instanceof InfixExpression) {
            visit((InfixExpression) node);
        } else if (node instanceof PrefixExpression) {
            visit((PrefixExpression) node);
        } else if (node instanceof PostfixExpression) {
            visit((PostfixExpression) node);
        } else if (node instanceof MethodInvocation) {
            visit((MethodInvocation) node);
        } else if (node instanceof InstanceofExpression) {
            visit((InstanceofExpression) node);
        } else if (node instanceof ParenthesizedExpression) {
            visit((ParenthesizedExpression) node);
        } else if (node instanceof FieldAccess) {
            visit((FieldAccess) node);
        } else if (node instanceof VariableDeclarationExpression) {
            visit((VariableDeclarationExpression) node);
        } else if (node instanceof VariableDeclarationFragment) {
            visit((VariableDeclarationFragment) node);
        } else if (node instanceof CastExpression) {
            visit((CastExpression) node);
        } else if (node instanceof ConditionalExpression) {
            visit((ConditionalExpression) node);
        } else if (node instanceof ArrayCreation) {
            visit((ArrayCreation) node);
        } else if (node instanceof ReturnStatement) {
            _nodeMap.put(index++, node);
            _numberPresentation.add(_identifier.getId(new IdentifierToken("return")));
        } else if (node instanceof SuperMethodInvocation) {
            visit((SuperMethodInvocation) node);
        } else if (node instanceof ClassInstanceCreation) {
            visit((ClassInstanceCreation) node);
        } else if (node instanceof ArrayInitializer) {
            visit((ArrayInitializer) node);
        } else {
            mapLeafNode(node);
        }
    }

    @Override
    public boolean visit(ArrayAccess arrayAccess) {
        map(arrayAccess.getArray());
        map(arrayAccess.getIndex());
        return false;
    }

    @Override
    public boolean visit(SuperMethodInvocation superMethodInvocation) {
        mapLeafNode(superMethodInvocation.getName());
        for (Object o : superMethodInvocation.arguments()) {
            map((ASTNode) o);
        }
        return false;
    }

    @Override
    public boolean visit(ArrayInitializer arrayInitializer) {
        for (Object o : arrayInitializer.expressions()) {
            map((ASTNode) o);
        }
        return false;
    }

    @Override
    public boolean visit(ClassInstanceCreation classInstanceCreation) {
        map(classInstanceCreation.getType());
        for (Object o : classInstanceCreation.arguments()) {
            map((ASTNode) o);
        }
        return false;
    }

    @Override
    public boolean visit(Assignment assignment) {
        map(assignment.getLeftHandSide());
        map(assignment.getRightHandSide());
        return false;
    }

    @Override
    public boolean visit(InstanceofExpression instanceofExpression) {
        map(instanceofExpression.getLeftOperand());
        //map(instanceofExpression.getRightOperand());
        return false;
    }

    @Override
    public boolean visit(ConditionalExpression conditionalExpression) {
        map(conditionalExpression.getExpression());
        map(conditionalExpression.getThenExpression());
        map(conditionalExpression.getElseExpression());
        return false;
    }

    @Override
    public boolean visit(ArrayCreation arrayCreation) {
        map(arrayCreation.getType());
        for (Object dimension : arrayCreation.dimensions()) {
            map((ASTNode) dimension);
        }
        if (arrayCreation.getInitializer() == null) {
            return false;
        }
        for (Object o : arrayCreation.getInitializer().expressions()) {
            map((ASTNode) o);
        }
        return false;
    }

    @Override
    public boolean visit(MethodInvocation methodInvocation) {
        map(methodInvocation.getExpression());
        mapLeafNode(methodInvocation.getName());
        for (Object o : methodInvocation.arguments()) {
            map((ASTNode) o);
        }
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationStatement vdStmt) {
        mapLeafNode(vdStmt.getType());
        for (Object o : vdStmt.fragments()) {
            map((ASTNode) o);
        }
        return false;
    }

    @Override
    public boolean visit(ReturnStatement returnStmt) {
        map(returnStmt);
        map(returnStmt.getExpression());
        return false;
    }

    @Override
    public boolean visit(CastExpression castExpression) {
        // We shall not change the cast type which could lead to a compiled fault.
        map(castExpression.getExpression());
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationExpression variableDeclarationExpression) {
        mapLeafNode(variableDeclarationExpression.getType());
        for (Object obj : variableDeclarationExpression.fragments()) {
            visit((VariableDeclarationFragment) obj);
        }
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationFragment variableDeclarationFragment) {
        map(variableDeclarationFragment.getName());
        map(variableDeclarationFragment.getInitializer());
        return false;
    }

    @Override
    public boolean visit(ForStatement forStatement) {
        for (Object obj : forStatement.initializers()) {
            map((ASTNode) obj);
        }
        map(forStatement.getExpression());
        for (Object obj : forStatement.updaters()) {
            map((ASTNode) obj);
        }
        return false;
    }

    @Override
    public boolean visit(ParenthesizedExpression parenthesizedExpression) {
        map(parenthesizedExpression.getExpression());
        return false;
    }

    @Override
    public boolean visit(InfixExpression infixExpr) {
        map(infixExpr.getLeftOperand());
        // Although the location is mapped to the infixExpr itself,It is only allowed to modify its
        // operator as the both left and right operand is separately mapped.
        mapLeafNode(infixExpr);
        map(infixExpr.getRightOperand());
        return false;
    }

    @Override
    public boolean visit(PrefixExpression prefixExpression) {
        mapLeafNode(prefixExpression);
        map(prefixExpression.getOperand());
        return false;
    }

    @Override
    public boolean visit(FieldAccess fieldAccess) {
        map(fieldAccess.getExpression());
        mapLeafNode(fieldAccess.getName());
        return false;
    }

    @Override
    public boolean visit(SimpleType simpleType) {
        map(simpleType);
        return false;
    }

    @Override
    public boolean visit(ThisExpression thisExpression) {
        mapLeafNode(thisExpression);
        return false;
    }

    @Override
    public boolean visit(PostfixExpression postfixExpression) {
        map(postfixExpression.getOperand());
        mapLeafNode(postfixExpression);
        return false;
    }

    @Override
    public boolean visit(SimpleName simpleName) {
        map(simpleName);
        return false;
    }

    @Override
    public boolean visit(BooleanLiteral booleanLiteral) {
        map(booleanLiteral);
        return false;
    }

    @Override
    public boolean visit(QualifiedName qualifiedName) {
        //map(qualifiedName);
        map(qualifiedName.getQualifier());
        map(qualifiedName.getName());
        return false;
    }

    @Override
    public boolean visit(NumberLiteral numberLiteral) {
        map(numberLiteral);
        return false;
    }

    @Override
    public boolean visit(StringLiteral stringLiteral) {
        map(stringLiteral);
        return false;
    }

    @Override
    public boolean visit(NullLiteral nullLiteral) {
        map(nullLiteral);
        return false;
    }

    @Override
    public boolean visit(CharacterLiteral characterLiteral) {
        map(characterLiteral);
        return false;
    }

    public Map<Integer, ASTNode> getMap() {
        return _nodeMap;
    }
}
