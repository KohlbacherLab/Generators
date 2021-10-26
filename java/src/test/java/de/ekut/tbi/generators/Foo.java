package de.ekut.tbi.generators;


import java.util.List;


public final class Foo {

  public enum Type {
    ONE,TWO,THREE,FOUR
  }

  public final int i;
  public final double d;
  public final Type t;
  public final List<String> s;

  public Foo(
    int i,
    double d,
    Type t,
    List<String> s
  ){
    this.i = i;
    this.d = d;
    this.t = t;
    this.s = s;
  }

  @Override
  public String toString(){
//    return "Foo(" + i + "," + d + "," + s + ")";
    return "Foo(" + i + "," + d + "," + t + "," + s + ")";
  }

}

