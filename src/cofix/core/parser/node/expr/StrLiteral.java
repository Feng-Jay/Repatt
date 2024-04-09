/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved. Unauthorized copying of this file via any
 * medium is strictly prohibited Proprietary and Confidential. Written by Jiajun
 * Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import cofix.core.metric.Literal;
import cofix.core.metric.NewFVector;
import cofix.core.metric.SVariable;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class StrLiteral extends Expr {

  private String _value = null;

  private final String _replace = null;

  /** String literal nodes. */
  public StrLiteral(int startLine, int endLine, ASTNode node) {
    super(startLine, endLine, node);
    _nodeType = TYPE.SLITERAL;
  }

  public void setValue(String value) {
    _value =
        value
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\b", "\\b")
            .replace("\t", "\\t")
            .replace("\r", "\\r")
            .replace("\f", "\\f")
            .replace("\0", "\\0");
  }

  @Override
  public boolean match(
      Node node,
      Map<String, String> varTrans,
      Map<String, Type> allUsableVariables,
      List<Modification> modifications) {
    boolean match = false;
    if (node instanceof StrLiteral) {
      match = true;
      //  OLDO : to finish
    } else {
      List<Node> children = node.getChildren();
      List<Modification> tmp = new ArrayList<>();
      if (NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)) {
        match = true;
        modifications.addAll(tmp);
      }
    }
    return match;
  }

  @Override
  public boolean adapt(Modification modification) {
    //  OLDO Auto-generated method stub
    return false;
  }

  @Override
  public boolean restore(Modification modification) {
    //  OLDO Auto-generated method stub
    return false;
  }

  @Override
  public boolean backup(Modification modification) {
    //  OLDO Auto-generated method stub
    return false;
  }

  @Override
  public StringBuffer toSrcString() {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("\"");
    if (_replace != null) {
      stringBuffer.append(_replace);
    } else {
      stringBuffer.append(_value);
    }
    stringBuffer.append("\"");
    return stringBuffer;
  }

  @Override
  public List<Literal> getLiterals() {
    List<Literal> list = new LinkedList<>();
    Literal literal = new Literal(this);
    list.add(literal);
    return list;
  }

  @Override
  public List<SVariable> getVariables() {
    return new LinkedList<>();
  }

  @Override
  public void computeFeatureVector() {
    _fVector = new NewFVector();
    _fVector.inc(NewFVector.INDEX_LITERAL);
  }

  @Override
  public List<Node> getChildren() {
    return new ArrayList<>();
  }

  @Override
  public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
    return toSrcString().toString();
  }
}
