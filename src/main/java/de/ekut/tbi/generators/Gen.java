package de.ekut.tbi.generators;


import java.util.Random;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
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
import java.lang.reflect.Modifier;



abstract class Gen<T>
{

   private Gen(){ }


   //--------------------------------------------------------------------------
   // Gen<T> class public interface
   //--------------------------------------------------------------------------
   public abstract T next(Random rnd);

   //TODO: re-consider this utility method, because at present not stack-safe 
   public Gen<T> filter(Predicate<T> p)
   {
     return apply(rnd -> doFilter(this,p,rnd));
   }

   private static <T> T doFilter(Gen<T> gen, Predicate<T> p, Random rnd){
     T t = gen.next(rnd);
     return p.test(t) ? t : doFilter(gen,p,rnd);
   }

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

   public static final Gen<Instant> intantNow(){ return INST_NOW; }


   private static final List<String> ALPHABET = Stream.of(
     "a","b","c","d","e", "A","B","C","D","E",
     "f","g","h","i","j", "F","G","H","I","J",
     "k","l","m","n","o", "K","L","M","N","O",
     "p","q","r","s","t", "P","Q","R","S","T",
     "u","v","w","x","y", "U","V","W","X","Y",
     "z",                 "Z"
   ).collect(toList());

   private static final List<String> DIGITS = Stream.of(
     "0","1","2","3","4","5","6","7","8","9" 
   ).collect(toList());


   private static final List<String> ALPHA_NUMERIC = Stream.concat(
     ALPHABET.stream(), DIGITS.stream()
   ).collect(toList());
   

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


   public static <T> Gen<Optional<T>> optional(Gen<T> gen){
     return apply(rnd -> Optional.of(rnd.nextBoolean())
                                 .filter(b -> b == true)
                                 .map(b -> gen.next(rnd)));
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
       Gen.lift(
         keys,
         values,
         Gen::entry
       )
     ).map(s -> s.limit(n).collect(toMap(Map.Entry::getKey,Map.Entry::getValue)));
   }


   public static <T> Gen<Stream<T>> stream(Gen<T> gen){
     return apply(rnd -> Stream.generate(() -> gen.next(rnd)));
   }


   @SafeVarargs
   public static <T> Gen<T> oneOf(T t1, T t2, T... ts){

     List<T> vals = Stream.concat(Stream.of(t1,t2),
                                  Stream.of(ts))
                          .collect(toList());
     return oneOf(vals);
   }

   public static <T> Gen<T> oneOf(Collection<T> ts){
     return apply(rnd -> ts.stream()
                           .skip(rnd.nextInt(ts.size()))
                           .findFirst()
                           .get());    
   }



   static final class Weighted<T>
   {
     public final T value;
     public final double weight;

     Weighted(T t, double w){
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


   @SafeVarargs
   public static <T> Gen<T> distribution(
     Weighted<T> wt1,
     Weighted<T> wt2,
     Weighted<T>... wts
   ){

     return distribution(
       Stream.concat(Stream.of(wt1,wt2),
                     Stream.of(wts))
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
     return longsBetween(
              start.toEpochMilli(),
              end.toEpochMilli()
            ).map(Instant::ofEpochMilli);
   }


   public static Gen<LocalDate> localDatesBetween
   (
     LocalDate start,
     LocalDate end
   ){
     return longsBetween(
              start.toEpochDay(),
              end.toEpochDay()
            ).map(LocalDate::ofEpochDay);
   }

   public static Gen<LocalTime> localTimesBetween
   (
     LocalTime start,
     LocalTime end
   ){
     return longsBetween(
              start.toNanoOfDay(),
              end.toNanoOfDay()
            ).map(LocalTime::ofNanoOfDay);
   }

   public static Gen<LocalDateTime> localDateTimesBetween
   (
     LocalDateTime start,
     LocalDateTime end
   ){
     return lift(
              Gen.localDatesBetween(start.toLocalDate(),
                                    end.toLocalDate()),
              Gen.localTimesBetween(start.toLocalTime(),
                                    end.toLocalTime()),
              (d,t) -> LocalDateTime.of(d,t)
            );
   }


   //--------------------------------------------------------------------------
   // Combinators to provide "for comprehension"-like syntax
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


   public static <A,B,T> Gen<T> lift
   (
     Gen<? extends A> genA,
     Gen<? extends B> genB,
     BiFunction<? super A,? super B,? extends T> f
   ){
     return genA.flatMap(a -> genB.map(b -> f.apply(a,b)));
   }


   public static <A,B,C,T> Gen<T> lift
   (
     Gen<? extends A> genA,
     Gen<? extends B> genB,
     Gen<? extends C> genC,
     Function3<? super A,? super B,? super C,? extends T> f
   ){
     return genA.flatMap(
              a -> genB.flatMap(
              b -> genC.map(
              c -> f.apply(a,b,c)))
            );
   }

   public static <A,B,C,D,T> Gen<T> lift
   (
     Gen<? extends A> genA,
     Gen<? extends B> genB,
     Gen<? extends C> genC,
     Gen<? extends D> genD,
     Function4<? super A,? super B,? super C,? super D,? extends T> f
   ){
     return genA.flatMap(
              a -> genB.flatMap(
              b -> genC.flatMap(
              c -> genD.map(
              d -> f.apply(a,b,c,d)))) 
            );
   }

   public static <A,B,C,D,E,T> Gen<T> lift
   (
     Gen<? extends A> genA,
     Gen<? extends B> genB,
     Gen<? extends C> genC,
     Gen<? extends D> genD,
     Gen<? extends E> genE,
     Function5<? super A,? super B,? super C,? super D,? super E,? extends T> f
   ){
     return genA.flatMap(
              a -> genB.flatMap(
              b -> genC.flatMap(
              c -> genD.flatMap(
              d -> genE.map(
              e -> f.apply(a,b,c,d,e))))) 
            );
   }

   public static <A,B,C,D,E,F,T> Gen<T> lift
   (
     Gen<? extends A> genA,
     Gen<? extends B> genB,
     Gen<? extends C> genC,
     Gen<? extends D> genD,
     Gen<? extends E> genE,
     Gen<? extends F> genF,
     Function6<? super A,
               ? super B,
               ? super C,
               ? super D,
               ? super E,
               ? super F,
               ? extends T> fn
   ){
     return genA.flatMap(
              a -> genB.flatMap(
              b -> genC.flatMap(
              c -> genD.flatMap(
              d -> genE.flatMap(
              e -> genF.map(
              f -> fn.apply(a,b,c,d,e,f)))))) 
            );
   }



   //--------------------------------------------------------------------------
   // Methods for automatic derivation of Gen<T> for a given Class<T>
   //--------------------------------------------------------------------------
   private static <K,V> Map.Entry<K,V> entry(K k, V v){
     return new SimpleImmutableEntry<>(k,v);
   }


   private static Map<Type,Gen<?>> DERIVED_GENS =
     Stream.of(entry(int.class,            INT),
               entry(double.class,         DOUBLE),
               entry(boolean.class,        BOOLEAN),
               entry(String.class,         IDENTIFIER),
               entry(java.util.UUID.class, UUID),
               entry(LocalDate.class,      LD_NOW),
               entry(LocalDateTime.class,  LDT_NOW)
//               entry(.class, ),
              )
              .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));



   public static <T> void register(Gen<T> gen, Class<T> c){
     DERIVED_GENS.put(c,gen);
   } 

   public static <T> Gen<T> of(Class<T> cl){
     return (Gen<T>)DERIVED_GENS.computeIfAbsent(cl, Gen::deriveFor);
   }

   private static <T> Gen<T> of(Type t){
     return (Gen<T>)DERIVED_GENS.computeIfAbsent(t, Gen::deriveFor);
   }

   private static Gen<?> deriveFor(Type t){
     return deriveFor((Class<?>)t);
   }

   private static Gen<?> deriveFor(Class<?> cl){

     Constructor<?> cons = Stream.of(cl.getDeclaredConstructors())
                                 .filter(c -> Modifier.isPublic(c.getModifiers()))
                                 .max((c1,c2) -> c1.getParameterCount() - c2.getParameterCount())
                                 .get();
      
     Type[] signature = cons.getGenericParameterTypes();

     List<Gen<?>> gens = Stream.of(signature)
                               .map(Gen::of)
                               .collect(toList());
           
     return apply(
       rnd -> {
         try {
           return cons.newInstance(gens.stream()
                                .map(g -> g.next(rnd))
                                .toArray());
         } catch (Exception ex){
            throw new RuntimeException(ex); 
         }
       }
     );
     
   }


}
