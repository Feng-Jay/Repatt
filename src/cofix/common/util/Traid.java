package cofix.common.util;

public class Traid<T1, T2, T3> {
  private T1 first;
  private T2 second;
  private T3 third;

  public Traid(T1 first, T2 second, T3 third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public T1 getFirst() {
    return this.first;
  }

  public T2 getSecond() {
    return this.second;
  }

  public T3 getThird() {
    return this.third;
  }

  public void setFirst(T1 fst) {
    this.first = fst;
  }

  public void setSecond(T2 snd) {
    this.second = snd;
  }

  public void setThird(T3 thrd) {
    this.third = thrd;
  }

  @Override
  public String toString() {
    return "<" + first.toString() + "," + second.toString() +"," + third.toString() + ">";
  }
}
