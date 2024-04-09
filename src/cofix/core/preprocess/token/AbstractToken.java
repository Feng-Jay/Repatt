package cofix.core.preprocess.token;

import cofix.common.util.Pair;
import cofix.core.modification.BuggyFile;
import com.sun.corba.se.spi.monitoring.StatisticMonitoredAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.plaf.nimbus.State;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class AbstractToken {

  public enum Type {
    SimpleName,
    QualifiedName,
    NumberLiteral,
    StringLiteral,
    InfixExpressionOperator,
    PostfixExpressionOperator,
    PrefixExpressionOperator,
    NullLiteral, CharacterLiteral, BooleanLiteral
  }

  protected String _name;
  protected String _propertyid;
  protected String _type;
  protected Type _ASTType;
  protected Class _elementClass;
  protected Class _nodeClass;
  protected static AST _ast = AST.newAST(AST.JLS8);

  private Set<ASTNode> _nodes = new HashSet<>();

  private Set<Statement> _stmts = new HashSet<>();


  public AbstractToken(ASTNode node) {
    _name = "";
    _type = "";
    StructuralPropertyDescriptor property = node.getLocationInParent();
    _nodeClass = property.getNodeClass();
    if (property.isChildListProperty()) {
      _elementClass = ((ChildListPropertyDescriptor) property).getElementType();
    } else if (property.isChildProperty()) {
      _elementClass = ((ChildPropertyDescriptor) property).getChildType();
    }
    _propertyid = property.getId();
  }

  public AbstractToken() {
    _name = "";
    _type = "";
  }

  public AbstractToken(String name, String type, Class elementClass) {
    _name = name;
    _type = type;
    _elementClass = elementClass;
  }

  public void recordOccurrence(ASTNode node) {
    _nodes.add(node);
  }

  public Set<Statement> resolveOccurrenceStmt() {
    if (!_stmts.isEmpty()) {
      return _stmts;
    }
    for (ASTNode node : _nodes) {
      while (!(node instanceof Statement)) {
        node = node.getParent();
        if (node == null) {
          break;
        }
      }
      if (node != null) {
        _stmts.add((Statement) node);
      }
    }
    return _stmts;
  }

  public Class getElementClass() {
    return _elementClass;
  }

  public Class getNodeClass() {
    return _nodeClass;
  }

  public String getName() {
    return _name;
  }

  public String getType() {
    return _type;
  }

  public boolean isCompatibleWith(AbstractToken token, Set<String> avaVars) {
    return (_type.equals(token.getType()) || token instanceof NullLiteralToken);
  }

  public Pair<ASTNode, Double> buildNode(ASTNode origin, BuggyFile buggyFile,
      Set<String> availableVars) throws Exception {
    return null;
  }

  public boolean isLeafNode() {
    return false;
  }

  public List<AbstractToken> makeMutations() {
    return null;
  }

  @Override
  public int hashCode() {
    return _name.hashCode() + _type.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AbstractToken)) {
      return false;
    }
    return ((AbstractToken) obj).getName().equals(_name) && ((AbstractToken) obj).getType()
        .equals(_type);
  }
}
