package de.ekut.tbi.generators;


import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;


abstract class Prop<T>
{

  private static final Random RND = new Random(42);
  private static final int N = 100;


  private final Predicate<T> p;
//  private final Gen<T> gen;


  private Prop(
//    Gen<T> gen,
    Predicate<T> p
  ){
    this.p = p;
//    this.gen = gen;
  }


  public Prop<T> or(Prop<T> other){
    return new Prop<>(this.p.or(other.p)){};
  }

  public Prop<T> and(Prop<T> other){
    return new Prop<>(this.p.and(other.p)){};
  }


//  public boolean check(){
  public boolean check(Gen<T> gen){
    return Stream.generate(() -> gen.next(RND))
                 .limit(N)
                 .allMatch(this.p);
  }


  public static <T> Prop<T> forAll(
//    Gen<T> gen,
    Predicate<T> p
  ){
//    return new Prop<>(gen,p){};
    return new Prop<>(p){};
  }



}
