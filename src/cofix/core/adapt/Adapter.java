/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved. Unauthorized copying of this file via any
 * medium is strictly prohibited Proprietary and Confidential. Written by Jiajun
 * Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.adapt;

import cofix.core.modify.Modification;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public interface Adapter {
  boolean adapt(Modification modification);

  boolean restore(Modification modification);

  boolean backup(Modification modification);
}
