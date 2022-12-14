package de.ekut.tbi.generators;


import java.util.Random;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import static java.util.AbstractMap.SimpleImmutableEntry;

import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import java.util.stream.Stream;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.joining;

import java.time.*;

import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static de.ekut.tbi.generators.Utils.*;


public abstract class Gen<T>
{

  private Gen(){ }


  //--------------------------------------------------------------------------
  // Gen<T> class public interface
  //--------------------------------------------------------------------------
  public abstract T next(Random rnd);


  //TODO: re-consider if this method is really safe -- and needed!
  public Gen<T> filter(Predicate<? super T> p)
  {
    return apply(
      rnd -> unfold(Stream.generate(() -> this.next(rnd))
                          .dropWhile(p.negate())).next(rnd)
    );
  }

/*
  //TODO: re-consider this utility method, because at present not stack-safe 
  public Gen<T> filter(Predicate<? super T> p)
  {
    return apply(rnd -> doFilter(this,p,rnd));
  }
  private static <T> T doFilter(Gen<T> gen, Predicate<T> p, Random rnd){
    T t = gen.next(rnd);
    return p.test(t) ? t : doFilter(gen,p,rnd);
  }
*/

  public <U> Gen<U> map(Function<? super T, ? extends U> f)
  {
    return apply(rnd -> f.apply(this.next(rnd)));
  }

  public <U> Gen<U> flatMap(Function<? super T, ? extends Gen<? extends U>> f)
  {
    return apply(rnd -> f.apply(this.next(rnd)).next(rnd));
  }


  //--------------------------------------------------------------------------
  // Gen constructor methods
  //--------------------------------------------------------------------------
  public static <T> Gen<T> supply(Supplier<? extends T> s)
  {
     return apply(rnd -> s.get());
  }

  public static <T> Gen<T> apply(Function<? super Random,? extends T> f)
  {
    return new Gen<>(){

      @Override
      public T next(Random rnd){
        return f.apply(rnd);
      }
    };
  }


  //--------------------------------------------------------------------------
  // Primitive/simple type constructors
  //--------------------------------------------------------------------------

  private static final Gen<Integer> INT           = apply(rnd -> rnd.nextInt());
  private static final Gen<Long>    LONG          = apply(rnd -> rnd.nextLong());
  private static final Gen<Float>   FLOAT         = apply(rnd -> rnd.nextFloat());
  private static final Gen<Double>  DOUBLE        = apply(rnd -> rnd.nextDouble());
  private static final Gen<Double>  GAUSSIANS     = apply(rnd -> rnd.nextGaussian());
  private static final Gen<Boolean> BOOLEAN       = apply(rnd -> rnd.nextBoolean());
  private static final Gen<java.util.UUID> UUID   = supply(java.util.UUID::randomUUID);
  private static final Gen<String> IDENTIFIER     = UUID.map(java.util.UUID::toString);
  private static final Gen<LocalDate> LD_NOW      = supply(LocalDate::now);
  private static final Gen<LocalDateTime> LDT_NOW = supply(LocalDateTime::now);
  private static final Gen<Instant> INST_NOW      = supply(Instant::now);

  public static final Gen<Integer> ints(){ return INT; } 

  public static final Gen<Double>  doubles(){ return DOUBLE; }     

  public static final Gen<Long> longs(){ return LONG; }

  public static final Gen<Float> floats(){ return FLOAT; }            

  public static final Gen<Boolean> booleans(){ return BOOLEAN; }

  public static final Gen<java.util.UUID> uuids(){ return UUID; }

  public static final Gen<String> uuidStrings(){ return IDENTIFIER; }   

  public static final Gen<LocalDate> localDateNow(){ return LD_NOW; }      

  public static final Gen<LocalDateTime> localDateTimeNow(){ return LDT_NOW; }

  public static final Gen<Instant> instantNow(){ return INST_NOW; }


  private static final List<String> ALPHABET =
    Stream.of(
      "a","b","c","d","e", "A","B","C","D","E",
      "f","g","h","i","j", "F","G","H","I","J",
      "k","l","m","n","o", "K","L","M","N","O",
      "p","q","r","s","t", "P","Q","R","S","T",
      "u","v","w","x","y", "U","V","W","X","Y",
      "z",                 "Z"
    )
    .collect(toList());

  private static final List<String> DIGITS =
    Stream.of(
      "0","1","2","3","4","5","6","7","8","9" 
    )
    .collect(toList());


  private static final List<String> ALPHA_NUMERIC =
    Stream.concat(
      ALPHABET.stream(), DIGITS.stream()
    )
    .collect(toList());
  

  public static Gen<String> letters(int length){
    return stream(oneOf(ALPHABET))
                 .map(s -> s.limit(length).collect(joining()));
  }  
 
  public static Gen<String> numeric(int length){
    return stream(oneOf(DIGITS))
                 .map(s -> s.limit(length).collect(joining()));
  }  
 
  public static Gen<String> alphaNumeric(int length){
    return stream(oneOf(ALPHA_NUMERIC))
                 .map(s -> s.limit(length).collect(joining()));
  }  
 

  public static Gen<Integer> intsBetween(int start, int endExcl){
    return iterate(new Random(42).ints(start, endExcl).iterator());
  }


  public static Gen<Long> longsBetween(long start, long endExcl){
    return iterate(new Random(42).longs(start, endExcl).iterator());
  }


  public static Gen<Double> doublesBetween(double start, double end){
    return iterate(new Random(42).doubles(start,end).iterator());
  }


  public static <T> Gen<T> constant(T t){
    return apply(rnd -> t);
  }


  public static <T> Gen<T> iterate(T seed, UnaryOperator<T> f){
    return unfold(Stream.iterate(seed,f));
  }


  public static <T> Gen<T> unfold(Stream<T> s){
    return iterate(s.iterator());
  }


  public static <T> Gen<T> iterate(Iterator<T> it){
    return apply(rnd -> it.next());
  }


  public static Gen<Integer> indicesFrom(int start){
    return iterate(start, i -> i+1);
  }


  public static Gen<Integer> indices(){
    return indicesFrom(0);
  }


  public static <T> Gen<Optional<T>> optional(
    Gen<T> gen,
    double p
  ){
    return apply(
      rnd -> rnd.nextDouble() < p ? Optional.of(gen.next(rnd))
                                  : Optional.empty()
    );
  }


  public static <T> Gen<Optional<T>> optional(
    Gen<T> gen
  ){
    return optional(gen, 0.5);
  }


  public static <T> Gen<List<T>> listOf(int n, Gen<T> gen){
    return stream(gen).map(s -> s.limit(n).collect(toList()));
  }

  public static <T> Gen<List<T>> list(
    Gen<Integer> sizes,
    Gen<T> gen
  ){
    return sizes.flatMap(s -> listOf(s,gen));
  }


  public static <T,C extends Collection<T>> Gen<C> collectionOf(
    Supplier<? extends C> sup,
    int n,
    Gen<T> gen
  ){
    return stream(gen).map(s -> s.limit(n)
                                 .collect(Collectors.toCollection(sup)));
  }

  public static <T,C extends Collection<T>> Gen<C> collection(
    Supplier<? extends C> sup,
    Gen<Integer> sizes,
    Gen<T> gen
  ){
    return sizes.flatMap(s -> collectionOf(sup,s,gen));
  }

  public static <K,V> Gen<Map<K,V>> mapOf(
    int n,
    Gen<K> keys,
    Gen<V> values
  ){
    return stream(
      Gen.given(
        keys,
        values
      )
      .map(Gen::entry)
    )
    .map(s -> s.limit(n).collect(toMap(Map.Entry::getKey,Map.Entry::getValue)));
  }


  public static <T> Gen<Stream<T>> stream(Gen<T> gen){
    return apply(rnd -> Stream.generate(() -> gen.next(rnd)));
  }


  @SafeVarargs
  public static <T> Gen<T> oneOf(T t1, T t2, T... ts)
  {

    List<T> vals =
      Stream.concat(Stream.of(t1,t2),Stream.of(ts))
        .collect(toList());

    return oneOf(vals);
  }

  public static <T> Gen<T> oneOf(Collection<T> ts){
    return apply(
      rnd -> ts.stream()
               .skip(rnd.nextInt(ts.size()))
               .findFirst()
               .get());    
  }

  public static <T extends Enum<T>> Gen<T> enumValues(Class<T> cl){
    return oneOf(Stream.of(cl.getEnumConstants()).collect(toList()));
  }


  public static <K,T> Gen<Map.Entry<K,T>> oneOf(Map<K,T> ts){
    return oneOf(ts.entrySet());
  }



  public static <T, Gs extends Collection<Gen<T>>, Cs extends Collection<T>> Gen<Cs> oneOfEach(
    Supplier<? extends Cs> sup,
    Gs gens
  ){
    return apply(
      rnd -> gens.stream().map(g -> g.next(rnd)).collect(Collectors.toCollection(sup))
    );
  }


  public static final class Weighted<T>
  {
    public final T value;
    public final double weight;

    private Weighted(T t, double w){
      this.value  = t;
      this.weight = w;
    }
  }


  private static final class Bin
  {
    public final double start;
    public final double end;

    private Bin(double start, double end){
      this.start = start;
      this.end = end;
    }

    boolean contains(double d){
      return d >= start && d < end;
    }
 
    private static Bin of(double start, double end){
      return new Bin(start,end);
    }
  }


  public static <T> Weighted<T> weighted(T t, double w){
    return new Weighted<>(t,w);
  }



  public static <T> Gen<T> distribution(
    T t1, double w1,
    T t2, double w2
  ){
    return distribution(
      weighted(t1,w1),
      weighted(t2,w2)
    );
  }

  public static <T> Gen<T> distribution(
    T t1, double w1,
    T t2, double w2,
    T t3, double w3
  ){
    return distribution(
      weighted(t1,w1),
      weighted(t2,w2),
      weighted(t3,w3)
    );
  }

  public static <T> Gen<T> distribution(
    T t1, double w1,
    T t2, double w2,
    T t3, double w3,
    T t4, double w4
  ){
    return distribution(
      weighted(t1,w1),
      weighted(t2,w2),
      weighted(t3,w3),
      weighted(t4,w4)
    );
  }

  public static <T> Gen<T> distribution(
    T t1, double w1,
    T t2, double w2,
    T t3, double w3,
    T t4, double w4,
    T t5, double w5
  ){
    return distribution(
      weighted(t1,w1),
      weighted(t2,w2),
      weighted(t3,w3),
      weighted(t4,w4),
      weighted(t5,w5)
    );
  }

  public static <T> Gen<T> distribution(
    T t1, double w1,
    T t2, double w2,
    T t3, double w3,
    T t4, double w4,
    T t5, double w5,
    T t6, double w6
  ){
    return distribution(
      weighted(t1,w1),
      weighted(t2,w2),
      weighted(t3,w3),
      weighted(t4,w4),
      weighted(t5,w5),
      weighted(t6,w6)
    );
  }

  @SafeVarargs
  public static <T> Gen<T> distribution(
    Weighted<T> wt1,
    Weighted<T> wt2,
    Weighted<T>... wts
  ){

    return distribution(
      Stream.concat(Stream.of(wt1,wt2), Stream.of(wts))
            .collect(toList())
    );
  }

  public static <T> Gen<T> distribution(
    Map<T,Double> valuesWeights
  ){
    return distribution(
      valuesWeights.entrySet()
        .stream()
        .map(e -> weighted(e.getKey(),e.getValue().doubleValue()))
        .collect(toList())
    );
  }


  public static <T> Gen<T> distribution(
    List<Weighted<T>> wts
  ){

    List<T>      ts = wts.stream().map(wt -> wt.value).collect(toList());
    List<Double> ws = wts.stream().map(wt -> wt.weight).collect(toList());

    double sumWs = ws.stream().reduce(0.0, (d1,d2) -> d1 + d2);

    List<Double> nws = ws.stream().map(w -> w/sumWs).collect(toList());

    List<Map.Entry<Bin,T>> binnedTs = new ArrayList<>();
                              
    double lowerBound = 0.0;

    for (int i = 0; i < nws.size(); i++){

       double upperBound = lowerBound + nws.get(i);
       T t = ts.get(i);

       binnedTs.add(entry(Bin.of(lowerBound,upperBound), t));
       lowerBound = upperBound;
    }

    return Gen.apply(
      rnd -> {
        double d = rnd.nextDouble();
        return binnedTs.stream()
                       .filter(bt -> bt.getKey().contains(d))
                       .findFirst()
                       .get()
                       .getValue();
      }
    );      

  }



  //--------------------------------------------------------------------------
  // DateTime type generators
  //--------------------------------------------------------------------------

  public static final Gen<DayOfWeek> DAYOFWEEK = Gen.oneOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
  ); 


  public static final Gen<Month> MONTH = Gen.oneOf
  (
    Month.JANUARY, Month.FEBRUARY, Month.MARCH,
    Month.APRIL,   Month.MAY,      Month.JUNE,
    Month.JULY,    Month.AUGUST,   Month.SEPTEMBER,
    Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER
  );


  public static Gen<Instant> instantsBetween
  (
    Instant start,
    Instant end
  ){
    return
      longsBetween(
        start.toEpochMilli(),
        end.toEpochMilli()
      )
      .map(Instant::ofEpochMilli);
  }


  public static Gen<LocalDate> localDatesBetween
  (
    LocalDate start,
    LocalDate end
  ){
    return
      longsBetween(
        start.toEpochDay(),
        end.toEpochDay()
      )
      .map(LocalDate::ofEpochDay);
  }

  public static Gen<LocalTime> localTimesBetween
  (
    LocalTime start,
    LocalTime end
  ){
    return
      longsBetween(
        start.toNanoOfDay(),
        end.toNanoOfDay()
      )
      .map(LocalTime::ofNanoOfDay);
  }

  public static Gen<LocalDateTime> localDateTimesBetween
  (
    LocalDateTime start,
    LocalDateTime end
  ){
    return
      given(
        Gen.localDatesBetween(start.toLocalDate(), end.toLocalDate()),
        Gen.localTimesBetween(start.toLocalTime(), end.toLocalTime())
      )
      .map(LocalDateTime::of);
  }



  //--------------------------------------------------------------------------
  // Utilities to provide "for comprehension"-like syntax
  //--------------------------------------------------------------------------

  @FunctionalInterface
  public interface Function3<A,B,C,T>{
    public T apply(A a, B b, C c);
  }

  @FunctionalInterface
  public interface Function4<A,B,C,D,T>{
    public T apply(A a, B b, C c, D d);
  }

  @FunctionalInterface
  public interface Function5<A,B,C,D,E,T>{
    public T apply(A a, B b, C c, D d, E e);
  }

  @FunctionalInterface
  public interface Function6<A,B,C,D,E,F,T>{
    public T apply(A a, B b, C c, D d, E e, F f);
  }

  @FunctionalInterface
  public interface Function7<A,B,C,D,E,F,G,T>{
    public T apply(A a, B b, C c, D d, E e, F f, G g);
  }

  @FunctionalInterface
  public interface Function8<A,B,C,D,E,F,G,H,T>{
    public T apply(A a, B b, C c, D d, E e, F f, G g, H h);
  }



  public static abstract class Comprehension2<A,B>
  {
    private Comprehension2(){};

    public abstract <T> Gen<T> map(BiFunction<? super A,? super B,T> f);
  }

  public static abstract class Comprehension3<A,B,C>
  {
    private Comprehension3(){};

    public abstract <T> Gen<T> map(Function3<? super A,? super B,? super C,T> f);
  }

  public static abstract class Comprehension4<A,B,C,D>
  {
    private Comprehension4(){};

    public abstract <T> Gen<T> map(Function4<? super A,? super B,? super C,? super D,T> f);
  }

  public static abstract class Comprehension5<A,B,C,D,E>
  {
    private Comprehension5(){};

    public abstract <T> Gen<T> map(Function5<? super A,? super B,? super C,? super D,? super E,T> f);
  }

  public static abstract class Comprehension6<A,B,C,D,E,F>
  {
    private Comprehension6(){};

    public abstract <T> Gen<T> map(Function6<? super A,? super B,? super C,? super D,? super E,? super F,T> f);
  }

  public static abstract class Comprehension7<A,B,C,D,E,F,G>
  {
    private Comprehension7(){};

    public abstract <T> Gen<T> map(Function7<? super A,? super B,? super C,? super D,? super E,? super F,? super G,T> f);
  }

  public static abstract class Comprehension8<A,B,C,D,E,F,G,H>
  {
    private Comprehension8(){};

    public abstract <T> Gen<T> map(Function8<? super A,? super B,? super C,? super D,? super E,? super F,? super G,? super H,T> f);
  }


  public static <A,B> Comprehension2<A,B> given
  (
    Gen<? extends A> genA,
    Gen<? extends B> genB
  ){
    return new Comprehension2<>(){
      @Override
      public <T> Gen<T> map(BiFunction<? super A,? super B,T> f){
        return genA.flatMap(a -> genB.map(b -> f.apply(a,b)));
      }
    };
  }


  public static <A,B,C> Comprehension3<A,B,C> given
  (
    Gen<? extends A> genA,
    Gen<? extends B> genB,
    Gen<? extends C> genC
  ){
    return new Comprehension3<>(){
      @Override
      public <T> Gen<T> map(Function3<? super A,? super B,? super C,T> f){
        return
          genA.flatMap(
            a -> genB.flatMap(
            b -> genC.map(
            c -> f.apply(a,b,c)))
          );
      }
    };
  }


  public static <A,B,C,D> Comprehension4<A,B,C,D> given
  (
    Gen<? extends A> genA,
    Gen<? extends B> genB,
    Gen<? extends C> genC,
    Gen<? extends D> genD
  ){
    return new Comprehension4<>(){
      @Override
      public <T> Gen<T> map(
        Function4<? super A,? super B,? super C,? super D,T> f
      ){
        return
          genA.flatMap(
            a -> genB.flatMap(
            b -> genC.flatMap(
            c -> genD.map(
            d -> f.apply(a,b,c,d))))
          );
      }
    };
  }


  public static <A,B,C,D,E> Comprehension5<A,B,C,D,E> given
  (
    Gen<? extends A> genA,
    Gen<? extends B> genB,
    Gen<? extends C> genC,
    Gen<? extends D> genD,
    Gen<? extends E> genE
  ){
    return new Comprehension5<>(){
      @Override
      public <T> Gen<T> map(
        Function5<? super A,? super B,? super C,? super D,? super E,T> f
      ){
        return
          genA.flatMap(
            a -> genB.flatMap(
            b -> genC.flatMap(
            c -> genD.flatMap(
            d -> genE.map(
            e -> f.apply(a,b,c,d,e)))))
          );
      }
    };
  }


  public static <A,B,C,D,E,F> Comprehension6<A,B,C,D,E,F> given
  (
    Gen<? extends A> genA,
    Gen<? extends B> genB,
    Gen<? extends C> genC,
    Gen<? extends D> genD,
    Gen<? extends E> genE,
    Gen<? extends F> genF
  ){
    return new Comprehension6<>(){
      @Override
      public <T> Gen<T> map(
        Function6<? super A,? super B,? super C,? super D,? super E,? super F,T> func
      ){
        return
          genA.flatMap(
            a -> genB.flatMap(
            b -> genC.flatMap(
            c -> genD.flatMap(
            d -> genE.flatMap(
            e -> genF.map(
            f -> func.apply(a,b,c,d,e,f))))))
          );
      }
    };
  }


  public static <A,B,C,D,E,F,G> Comprehension7<A,B,C,D,E,F,G> given
  (
    Gen<? extends A> genA,
    Gen<? extends B> genB,
    Gen<? extends C> genC,
    Gen<? extends D> genD,
    Gen<? extends E> genE,
    Gen<? extends F> genF,
    Gen<? extends G> genG
  ){
    return new Comprehension7<>(){
      @Override
      public <T> Gen<T> map(
        Function7<? super A,? super B,? super C,? super D,? super E,? super F,? super G,T> func
      ){
        return
          genA.flatMap(
            a -> genB.flatMap(
            b -> genC.flatMap(
            c -> genD.flatMap(
            d -> genE.flatMap(
            e -> genF.flatMap(
            f -> genG.map(
            g -> func.apply(a,b,c,d,e,f,g)))))))
          );
      }
    };
  }


  public static <A,B,C,D,E,F,G,H> Comprehension8<A,B,C,D,E,F,G,H> given
  (
    Gen<? extends A> genA,
    Gen<? extends B> genB,
    Gen<? extends C> genC,
    Gen<? extends D> genD,
    Gen<? extends E> genE,
    Gen<? extends F> genF,
    Gen<? extends G> genG,
    Gen<? extends H> genH
  ){
    return new Comprehension8<>(){
      @Override
      public <T> Gen<T> map(
        Function8<? super A,? super B,? super C,? super D,? super E,? super F,? super G,? super H,T> func
      ){
        return
          genA.flatMap(
            a -> genB.flatMap(
            b -> genC.flatMap(
            c -> genD.flatMap(
            d -> genE.flatMap(
            e -> genF.flatMap(
            f -> genG.flatMap(
            g -> genH.map(
            h -> func.apply(a,b,c,d,e,f,g,h))))))))
          );
      }
    };
  }




  //--------------------------------------------------------------------------
  // Methods for automatic derivation of Gen<T> for a given Class<T>
  //--------------------------------------------------------------------------
  private static <K,V> Map.Entry<K,V> entry(K k, V v){
    return new SimpleImmutableEntry<>(k,v);
  }


  private static Map<Type,Gen<?>> DERIVED_GENS =
    Stream.of(
      entry(Integer.class,        INT.map(Integer::valueOf)),
      entry(Double.class,         DOUBLE.map(Double::valueOf)),
      entry(Float.class,          FLOAT.map(Float::valueOf)),
      entry(int.class,            INT),
      entry(long.class,           LONG),
      entry(float.class,          FLOAT),
      entry(double.class,         DOUBLE),
      entry(boolean.class,        BOOLEAN),
      entry(String.class,         Gen.constant("Lorem ipsum dolor sit amet, consectetur adipisici elit...")),
      entry(java.util.UUID.class, UUID),
      entry(LocalDate.class,      LD_NOW),
      entry(LocalDateTime.class,  LDT_NOW),
      entry(Instant.class,        INST_NOW)
    )
    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));



  public static <T> void register(Gen<? extends T> gen, Class<? extends T> c){
    DERIVED_GENS.put(c,gen);
  } 


  public static <T> Gen<T> deriveFor(Class<? extends T> cl, Map<Type,Gen<?>> defaultGens){
    return (Gen<T>)deriveForImpl(cl, Optional.of(defaultGens).filter(m -> !m.isEmpty()));
  }

  public static <T> Gen<T> deriveFor(Class<? extends T> cl){
     return (Gen<T>)deriveForImpl(cl, Optional.empty());
  }

  private static <T> Gen<T> deriveFor(Type t, Optional<Map<Type,Gen<?>>> defaultGens){
    return (Gen<T>)deriveForImpl(t, defaultGens);
  }

  private static <T> Gen<T> deriveFor(Type t){
    return (Gen<T>)deriveForImpl(t, Optional.empty());
  }


  private static final Map<Class<? extends Collection>,Class<? extends Collection>> DEFAULT_COLLECTION_CLASSES =
    Stream.of(
      entry(List.class, ArrayList.class),
      entry(AbstractList.class, ArrayList.class),
      entry(AbstractSet.class, HashSet.class),
      entry(Set.class,  HashSet.class)
    )
    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));


  private static Gen<?> deriveForImpl(Type t, Optional<Map<Type,Gen<?>>> gens){

    // Case: Type is some parameterized type C<T>
    if (isParameterized(t)){

      Class<?> rawType = getRawType(t);

      // Case: Type C<T> is Optional<T>
      if (isOptional(t)){

        Gen<?> genT = deriveFor(getParameterType(t), gens); 

        return Gen.optional(genT); 

      // Case: Type C<T> is some Collection<T>
      } else if (isCollection(t)){

        Gen<?> genT = deriveFor(getParameterType(t), gens); 

        try {
       
          Constructor<? extends Collection> con =
            DEFAULT_COLLECTION_CLASSES.getOrDefault((Class<? extends Collection>)rawType, (Class<? extends Collection>)rawType)
              .getConstructor();
 
          return Gen.apply(
            rnd -> {
              try {
                Collection coll = con.newInstance();
                for (int i = 0; i < intsBetween(2,10).next(rnd); i++){
                  coll.add(genT.next(rnd));
                }
                return coll;
              } catch (Exception e){
                throw new RuntimeException(e);
              }
            }
          );

        } catch (Exception e){
          throw new RuntimeException(e);
        }

      //  Case: Type is some Map<K,V>
      } else if (isMap(t)){

        Gen<?> genKey = deriveFor(getParameterType(t,0), gens); 
        Gen<?> genVal = deriveFor(getParameterType(t,1), gens); 

        try {

          Class<? extends Map> mapClass =
            rawType.isInterface() ? (Class<? extends Map>)HashMap.class : (Class<? extends Map>)rawType;

          Constructor<? extends Map> cons = mapClass.getConstructor();

          return Gen.apply(
            rnd -> {
              try {
                Map map = mapClass.newInstance();
                for (int i = 0; i < intsBetween(2,10).next(rnd); i++){
                  map.put(genKey.next(rnd),genVal.next(rnd));
                }
                return map;
              } catch (Exception e){
                throw new RuntimeException(e);
              }
            }
          );

        } catch (Exception e){
          throw new RuntimeException(e);
        }

      } else {   
        throw new IllegalStateException("Unsupported case for Generator derivation of parameterized type: " + t);
      }

    // Case: Any other type expected 'normal'
    } else {
      return deriveForImpl(Class.class.cast(t), gens);
    }

  }


  private static <T> Gen<T> deriveForImpl(Class<? extends T> cl, Optional<Map<Type,Gen<?>>> defaultGens){
  
    if (cl.isEnum()){

      Gen<?> enumGen =
        defaultGens.isPresent() ?
          defaultGens.get().getOrDefault(cl,oneOf(Stream.of(cl.getEnumConstants()).collect(toList()))) :
          oneOf(Stream.of(cl.getEnumConstants()).collect(toList()));

      return (Gen<T>)enumGen;

    } else {

      return
        defaultGens
          .flatMap(g -> Optional.ofNullable((Gen<T>)g.get(cl)))
          .orElseGet(
            () ->
              (Gen<T>)DERIVED_GENS.computeIfAbsent(
                cl, c -> buildGenFor(Class.class.cast(c),defaultGens)
              )
          );
    }
  }


  private static <T> Gen<T> buildGenFor(Class<? extends T> cl, Optional<Map<Type,Gen<?>>> defaultGens){

    // 1. Strategy: Look up non-default constructor with longest parameter signature
    //              to generate T instances accordingly
    Optional<Constructor<?>> nonDefaultCons =
      Stream.of(cl.getConstructors())
        .filter(c -> c.getParameterCount() > 0)
        .max((c1,c2) -> c1.getParameterCount() - c2.getParameterCount());
  
    if (nonDefaultCons.isPresent()){
  
      Constructor<?> cons = nonDefaultCons.get();
    
      Type[] signature = cons.getGenericParameterTypes();

    
      Gen<Object[]> genArgs =
        oneOfEach(
          ArrayList::new,
          Stream.of(signature)
            .map(t -> deriveFor(t, defaultGens))
            .collect(toList())
        )
        .map(List::toArray);
            
      return (Gen<T>)apply(
        rnd -> {
          try {
            return cons.newInstance(genArgs.next(rnd));
          } catch (Exception ex){
             throw new RuntimeException(ex); 
          }
        }
      );

/*  
      List<Gen<?>> gens =
        Stream.of(signature)
              .map(t -> deriveFor(t, defaultGens))
              .collect(toList());

      return (Gen<T>)apply(
        rnd -> {
          try {
            return cons.newInstance(gens.stream().map(g -> g.next(rnd)).toArray());
          } catch (Exception ex){
             throw new RuntimeException(ex); 
          }
        }
      );
*/  
    } else {
  
      // 2. Strategy: Look up default constructor and list of setter methods
      //              to create empty instances and invoke the setters successively
      try {
  
        Constructor<?> defaultCons = cl.getConstructor();
        
        List<Map.Entry<Method,Gen<?>>> settersGens =
          Stream.of(cl.getMethods())
            .filter(m -> m.getName().startsWith("set") && m.getParameterCount() == 1)
            .map(m -> Gen.<Method,Gen<?>>entry(m,deriveFor(m.getGenericParameterTypes()[0], defaultGens)))
            .collect(toList());
           
        return (Gen<T>)apply(
          rnd -> {
            try {
              Object obj = defaultCons.newInstance();
        
              for (Map.Entry<Method,Gen<?>> entry : settersGens){
                Method m = entry.getKey();
                Object v = entry.getValue().next(rnd);
        
                m.invoke(obj,v);   
              }
              return obj;
        
            } catch (Exception ex){
               throw new RuntimeException(ex); 
            }
          } 
        );

      } catch (NoSuchMethodException ex){
  
        // 3. Strategy: Look up static builder method with longest parameter signature
        //              to create T instance
  
        Optional<Method> optBuilder =
          Stream.of(cl.getMethods())
            .filter(m -> Modifier.isStatic(m.getModifiers()) && 
                         m.getParameterCount() > 0 &&
                         m.getReturnType().equals(cl))
            .max((m1,m2) -> m1.getParameterCount() - m2.getParameterCount());
            
        if (optBuilder.isPresent()){
  
          Method builder = optBuilder.get();
  
          Type[] signature = builder.getGenericParameterTypes();
          
          List<Gen<?>> gens =
            Stream.of(signature)
                  .map(c -> deriveFor(c, defaultGens))
                  .collect(toList());
                
          return (Gen<T>)apply(
            rnd -> {
              try {
                return builder.invoke(null, gens.stream().map(g -> g.next(rnd)).toArray());
              } catch (Exception e){
                 throw new RuntimeException(e); 
              }
            }
          );
  
        } else {

          // 4. Strategy: Look up static parameter-less builder method to create T instance
  
          Optional<Method> optBuilder2 =
            Stream.of(cl.getMethods())
              .filter(m -> Modifier.isPublic(m.getModifiers()) &&
                           Modifier.isStatic(m.getModifiers()) && 
                           m.getParameterCount() == 0 &&
                           m.getReturnType().equals(cl))
              .findFirst();
              
          if (optBuilder2.isPresent()){
        
            Method builder = optBuilder2.get();
        
            return (Gen<T>)supply(
              () -> {
                try {
                  return builder.invoke(null);
                } catch (Exception e){
                   throw new RuntimeException(e); 
                }
              }
            );
        
          } else {
            throw new RuntimeException(ex); 
          }
        }
      }
    }
  }

}
