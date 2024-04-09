/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved. Unauthorized copying of this file via any
 * medium is strictly prohibited Proprietary and Confidential. Written by Jiajun
 * Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.search;

import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * @author Jiajun
 * @date Jun 29, 2017
 */
public class BuggyCode {

    public static CodeBlock getBuggyCodeBlock(String fileName, int buggyLine) {
        CompilationUnit unit = JavaFile.genASTFromFileWithType(fileName);
        //		FindNodeVisitor visitor = new FindNodeVisitor(unit, buggyLine);
        //		unit.accept(visitor);
        //		CodeBlock codeBlock = new CodeBlock(fileName, unit, visitor.getNodes());
        CodeSearch codeSearch = new CodeSearch(unit, buggyLine, 5, null, 0);
        List<ASTNode> list = getNodeList(codeSearch.getExtendedStatement(), 3, 3);
        if (NodeUtils.getValidLineNumber(codeSearch.getExtendedStatement()) > 10) {
            return null;
        }
        CodeBlock codeBlock = new CodeBlock(fileName, unit, list);
        return codeBlock;
    }

    public static List<ASTNode> getNodeList(Statement node, int preMaxLine, int sufMaxLine) {
        List<ASTNode> list = new ArrayList<>();
        ASTNode parent = node;
        if (node == null) {
            return list;
        }
        while (!(parent instanceof Block)) {
            parent = parent.getParent();
        }
        Block block = (Block) parent;
//        int line=0;
//        for (Object object : block.statements()) {
//            line += NodeUtils.getValidLineNumber((ASTNode) object);
//        }
//        if(line<=maxLine){}
        int preLine = 0;
        int sufLine = 0;
        int index = block.statements().indexOf(node);
        for (int i = 0; i < index; i++) {
            ASTNode cur = (ASTNode) block.statements().get(i);
            preLine += NodeUtils.getValidLineNumber(cur);
            if (preLine <= preMaxLine) {
                list.add(cur);
            } else {
                break;
            }
        }
        list.add(node);
        for (int i = index + 1; i < block.statements().size(); i++) {
            ASTNode cur = (ASTNode) block.statements().get(i);
            sufLine += NodeUtils.getValidLineNumber(cur);
            if (sufLine <= sufMaxLine) {
                list.add(cur);
            } else {
                break;
            }
        }
        return list;
    }

    private static class FindNodeVisitor extends ASTVisitor {

        private CompilationUnit _unit = null;
        private int _buggyLine = 0;
        private final List<ASTNode> _nodes = new ArrayList<>();

        public FindNodeVisitor(CompilationUnit unit, int buggyLine) {
            _unit = unit;
            _buggyLine = buggyLine;
        }

        public List<ASTNode> getNodes() {
            return _nodes;
        }

        public boolean visit(CompilationUnit node) {

            //			int position = _unit.getPosition(_buggyLine, 0);
            //			NodeFinder finder = new NodeFinder(_unit, position, 20);
            //			ASTNode prefind = finder.getCoveringNode();
            //			while (prefind != null && !(prefind instanceof Statement)) {
            //				prefind = prefind.getParent();
            //			}
            //
            //			if(prefind != null){
            //				process((Statement)prefind);
            //				return false;
            //			}
            return true;
        }

        public boolean visit(MethodDeclaration node) {

            int start = _unit.getLineNumber(node.getStartPosition());
            int end = _unit.getLineNumber(node.getStartPosition() + node.getLength());
            if (start <= _buggyLine && _buggyLine <= end) {
                Statement statement = node.getBody();
                if (statement == null) {
                    return false;
                }
                process(statement);
                return false;
            }

            return true;
        }

        public boolean process(Statement statement) {

            //  OLDO : wait for completing ...

            int start = _unit.getLineNumber(statement.getStartPosition());
            int end = _unit.getLineNumber(statement.getStartPosition() + statement.getLength());

            if (start <= _buggyLine && _buggyLine <= end) {
                if (statement instanceof IfStatement
                        || statement instanceof ForStatement
                        || statement instanceof WhileStatement
                        || statement instanceof DoStatement
                        || statement instanceof EnhancedForStatement) {
                    _nodes.add(statement);
                    return false;
                } else if (statement instanceof Block) {
                    if (statement.getParent() instanceof IfStatement
                            && (end - start) < Constant.MAX_BLOCK_LINE) {
                        _nodes.add(statement.getParent());
                        return true;
                    }
                    Block block = (Block) statement;
                    for (Object object : block.statements()) {
                        process((Statement) object);
                    }
                } else if (statement instanceof SwitchStatement) {
                    SwitchStatement switchStmt = (SwitchStatement) statement;
                    for (int i = 0; i < switchStmt.statements().size(); i++) {
                        Statement stmt = (Statement) switchStmt.statements().get(i);
                        int s = _unit.getLineNumber(stmt.getStartPosition());
                        int e = _unit.getLineNumber(stmt.getStartPosition() + stmt.getLength());
                        if (s <= _buggyLine && _buggyLine <= e) {
                            _nodes.add(stmt);
                            if (stmt instanceof SwitchCase) {
                                for (int j = i + 1; j < switchStmt.statements().size(); j++) {
                                    Statement SC = (Statement) switchStmt.statements().get(j);
                                    if (SC instanceof SwitchCase) {
                                        return false;
                                    } else {
                                        _nodes.add(SC);
                                    }
                                }
                            } else {
                                _nodes.add(stmt);
                                return false;
                            }
                        }
                    }

                } else {
                    _nodes.add(statement);
                    return false;
                }
            }

            return true;
        }
    }
}
