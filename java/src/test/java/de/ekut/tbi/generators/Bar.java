package de.ekut.tbi.generators;


import java.util.Map;


public final class Bar {

  private int i;
  private double d;
  private Map<String,String> s;

  public Bar(){ }

  public Bar setInt(int i){
    this.i = i;
    return this;
  }

  public int getInt(){
    return this.i;
  }

  public Bar setDouble(double d){
    this.d = d;
    return this;
  }

  public Bar setStrings(final Map<String,String> s){
    this.s = s;
    return this;
  }

  @Override
  public String toString(){
    return "Bar(" + i + "," + d + "," + s + ")";
  }

}
