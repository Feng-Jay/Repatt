/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved. Unauthorized copying of this file via any
 * medium is strictly prohibited Proprietary and Confidential. Written by Jiajun
 * Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.util;

public class Pair<T1, T2> implements Cloneable{

  private T1 first;
  private T2 second;

  public Pair() {}

  public Pair(T1 fst, T2 snd) {
    this.first = fst;
    this.second = snd;
  }

  public T1 getFirst() {
    return this.first;
  }

  public T2 getSecond() {
    return this.second;
  }

  public void setFirst(T1 fst) {
    this.first = fst;
  }

  public void setSecond(T2 snd) {
    this.second = snd;
  }

  @Override
  public String toString() {
    return "<" + first.toString() + "," + second.toString() + ">";
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Pair)) return false;
    return first.toString().equals(((Pair<?, ?>) o).getFirst().toString()) && second.toString().equals(((Pair<?, ?>) o).getSecond().toString());
  }

  @Override
  public int hashCode() {
    return (first.toString()+second.toString()).hashCode();
  }

  @Override
  public Pair<T1, T2> clone() {
    try {
      Pair clone = (Pair) super.clone();
      // TODO: copy mutable state here, so the clone can't change the internals of the original
      clone.first = this.first;
      clone.second = this.second;
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
