package de.ekut.tbi.generators;


import java.util.Random;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.AbstractMap.SimpleImmutableEntry;

import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.*;

import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;



abstract class Gen<T>
{

   private Gen(){ }


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


   //--------------------------------------------------------------------------
   // Gen constructor methods
   //--------------------------------------------------------------------------
   public static <T> Gen<T> supply(Supplier<? extends T> s)
   {
      return apply(rnd -> s.get());
   }

   public static <T> Gen<T> apply(Function<? super Random,? extends T> f)
   {
     return new Gen<T>(){

       @Override
       public T next(Random rnd){
         return f.apply(rnd);
       }
     };
   }


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
   //--------------------------------------------------------------------------

/*  
   public static final Gen<Integer> INT     = apply(rnd -> rnd.nextInt());

   public static final Gen<Double>  DOUBLE  = apply(rnd -> rnd.nextDouble());

   public static final Gen<Long>    LONG    = apply(rnd -> rnd.nextLong());

   public static final Gen<Float>   FLOAT   = apply(rnd -> rnd.nextFloat());

   public static final Gen<Boolean> BOOLEAN = apply(rnd -> rnd.nextBoolean());

   public static final Gen<java.util.UUID> UUID = apply(rnd -> java.util.UUID.randomUUID());

   public static final Gen<String> IDENTIFIER = UUID.map(uuid -> uuid.toString());

   public static final Gen<LocalDate> LOCALDATE_NOW = apply(rnd -> LocalDate.now());

   public static final Gen<LocalDateTime> LOCALDATETIME_NOW = apply(rnd -> LocalDateTime.now());

   public static final Gen<Instant> INSTANT_NOW = apply(rnd -> Instant.now());
*/   
   
   private static final Gen<Integer> INT           = apply(rnd -> rnd.nextInt());
   private static final Gen<Double>  DOUBLE        = apply(rnd -> rnd.nextDouble());
   private static final Gen<Long>    LONG          = apply(rnd -> rnd.nextLong());
   private static final Gen<Float>   FLOAT         = apply(rnd -> rnd.nextFloat());
   private static final Gen<Boolean> BOOLEAN       = apply(rnd -> rnd.nextBoolean());
   private static final Gen<java.util.UUID> UUID   = supply(java.util.UUID::randomUUID);
   private static final Gen<String> IDENTIFIER     = UUID.map(uuid -> uuid.toString());
   private static final Gen<LocalDate> LD_NOW      = supply(LocalDate::now);
   private static final Gen<LocalDateTime> LDT_NOW = supply(LocalDateTime::now);
   private static final Gen<Instant> INST_NOW      = supply(Instant::now);
   

   public static final Gen<Integer> ints(){ return INT; } 

   public static final Gen<Double>  doubles(){ return DOUBLE; }     

   public static final Gen<Long> longs(){ return LONG; }

   public static final Gen<Float> floats(){ return FLOAT; }            

   public static final Gen<Boolean> booleans(){ return BOOLEAN; }

   public static final Gen<java.util.UUID> uuids(){ return UUID; }

   public static final Gen<String> idStrings(){ return IDENTIFIER; }   

   public static final Gen<LocalDate> localDateNow(){ return LD_NOW; }      

   public static final Gen<LocalDateTime> localDateTimeNow(){ return LDT_NOW; }

   public static final Gen<Instant> intantNow(){ return INST_NOW; }
   

   public static Gen<Integer> between(int start, int endExcl){
/*
     if (endExcl < start){
        throw new IllegalArgumentException("Interval upper bound must be larger that lower bound");
     }
*/
     return iterate(new Random(42).ints(start, endExcl).iterator());
//     return apply(rnd -> { return rnd.nextInt(endExcl-start) + start; } );
   }


   public static Gen<Long> between(long start, long endExcl){
/*
     if (endExcl < start){
        throw new IllegalArgumentException("Interval upper bound must be larger that lower bound");
     }
*/
     return iterate(new Random(42).longs(start, endExcl).iterator());
   }


   public static Gen<Double> between(double start, double end){
/*
     if (end < start){
        throw new IllegalArgumentException("Interval upper bound must be larger that lower bound");
     }
*/
     return iterate(new Random(42).doubles(start,end).iterator());
//     return apply(rnd -> { return rnd.nextDouble()*(end-start) + start; } );
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


   public static Gen<Integer> index(int start){
     return iterate(start, i -> i+1);
   }


   public static Gen<Integer> index(){
     return index(0);
   }


   public static <T> Gen<Optional<T>> optional(Gen<T> gen){
     return apply(rnd -> Optional.of(rnd.nextBoolean())
                                 .filter(b -> b == true)
                                 .map(b -> gen.next(rnd)));
   }


   public static <T> Gen<List<T>> listOf(int n, Gen<T> gen){
     return stream(gen).map(s -> s.limit(n).collect(toList()));
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

/*
   static final class P<T>
   {
     public final int frequency;
     public final T value;

     P(int f, T t){
       this.frequency = f;
       this. value    = t;
     }
   }

   public static <T> P<T> p(int f, T t){
     return new P<>(f,t);
   }

   public static <T> Gen<T> oneOfDistribution(P<T> p1, P<T> p2, P<T>... ps){

throw new RuntimeException("TODO");
   }
*/


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


   public static final Gen<Month> MONTH = Gen.oneOf(
     Month.JANUARY, Month.FEBRUARY, Month.MARCH,
     Month.APRIL,   Month.MAY,      Month.JUNE,
     Month.JULY,    Month.AUGUST,   Month.SEPTEMBER,
     Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER
   );

   public static Gen<LocalDate> between(LocalDate start, LocalDate end){
      return between(start.toEpochDay(),
                     end.toEpochDay()).map(LocalDate::ofEpochDay);
   }


   //--------------------------------------------------------------------------
   // Combinators to provide "for comprehension"-like syntax
   //--------------------------------------------------------------------------
   public static <A,B,T> Gen<T> lift
   (
     Gen<A> genA,
     Gen<B> genB,
     BiFunction<A,B,T> f
   ){
     return genA.flatMap(a -> genB.map(b -> f.apply(a,b)));
   }


   public static <A,B,C,T> Gen<T> lift
   (
     Gen<A> genA,
     Gen<B> genB,
     Gen<C> genC,
     Function3<A,B,C,T> f
   ){
     return genA.flatMap(
              a -> genB.flatMap(
              b -> genC.map(
              c -> f.apply(a,b,c)))
            );
   }

   public static <A,B,C,D,T> Gen<T> lift
   (
     Gen<A> genA,
     Gen<B> genB,
     Gen<C> genC,
     Gen<D> genD,
     Function4<A,B,C,D,T> f
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
     Gen<A> genA,
     Gen<B> genB,
     Gen<C> genC,
     Gen<D> genD,
     Gen<E> genE,
     Function5<A,B,C,D,E,T> f
   ){
     return genA.flatMap(
              a -> genB.flatMap(
              b -> genC.flatMap(
              c -> genD.flatMap(
              d -> genE.map(
              e -> f.apply(a,b,c,d,e))))) 
            );
   }



   //--------------------------------------------------------------------------
   // Methods for automatic derivation of Gen<T> for a given Class<T>
   //--------------------------------------------------------------------------
   private static <K,V> Map.Entry<K,V> entry(K k, V v){
     return new SimpleImmutableEntry<>(k,v);
   }


   private static Map<Type,Gen<?>> BASIC_GENS =
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
     BASIC_GENS.put(c,gen);
   } 

   public static <T> Gen<T> of(Class<T> cl){
     return (Gen<T>)BASIC_GENS.computeIfAbsent(cl, Gen::deriveFor);
   }

   private static <T> Gen<T> of(Type t){
     return (Gen<T>)BASIC_GENS.computeIfAbsent(t, Gen::deriveFor);
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
