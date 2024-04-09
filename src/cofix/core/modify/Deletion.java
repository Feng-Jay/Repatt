/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved. Unauthorized copying of this file via any
 * medium is strictly prohibited Proprietary and Confidential. Written by Jiajun
 * Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.modify;

import cofix.core.parser.node.Node;
import cofix.core.parser.node.Node.TYPE;

/**
 * @author Jiajun
 * @date Jun 30, 2017
 */
public class Deletion extends Modification {

  public Deletion(Node node, int srcID, String target, TYPE changeNodeType) {
    super(node, srcID, target, changeNodeType, -1);
    //  OLDO Auto-generated constructor stub
  }

  @Override
  public String toString() {
    return "[DEL | " + _nodeType + " | " + _sourceID + "]" + _node.toString().replace("\n", " ");
  }
}
