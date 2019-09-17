package de.ekut.tbi.generators;


import java.util.Random;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import java.util.function.Function;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;

import java.time.*;



abstract class Gen<T>
{

   Gen(){ }


   public static <T> Gen<T> build(Function<Random,? extends T> f)
   {
       return new Gen<T>(){
           public T next(Random rnd){
               return f.apply(rnd);
           }
       };
   }



   public abstract T next(Random rnd);
 
   public <U> Gen<U> map(Function<? super T, ? extends U> f)
   {
      return build(rnd -> f.apply(this.next(rnd)));
   }

   public <U> Gen<U> flatMap(Function<? super T, Gen<U>> f)
   {
      return build(rnd -> f.apply(this.next(rnd)).next(rnd));
   }


   public static final Gen<Integer> INT     = build(rnd -> rnd.nextInt());

   public static final Gen<Double>  DOUBLE  = build(rnd -> rnd.nextDouble());

   public static final Gen<Long>    LONG    = build(rnd -> rnd.nextLong());

   public static final Gen<Float>   FLOAT   = build(rnd -> rnd.nextFloat());

   public static final Gen<Boolean> BOOLEAN = build(rnd -> rnd.nextBoolean());

   public static final Gen<java.util.UUID> UUID = build(rnd -> java.util.UUID.randomUUID());

   public static final Gen<String> IDENTIFIER = UUID.map(uuid -> uuid.toString());

   public static final Gen<LocalDate> LOCALDATE_NOW = build(rnd -> LocalDate.now());

   public static final Gen<LocalDateTime> LOCALDATETIME_NOW = build(rnd -> LocalDateTime.now());

   public static final Gen<Instant> INSTANT_NOW = build(rnd -> Instant.now());
   

   public static Gen<Integer> between(int start, int endExcl){

       if (endExcl < start){
          throw new IllegalArgumentException("Interval upper bound must be larger that lower bound");
       }

       return build(rnd -> { return rnd.nextInt(endExcl-start) + start; } );
   }

   public static <T> Gen<T> constant(T t){
       return build(rnd -> t);
   }


   public static <T> Gen<List<T>> listOf(int n, Gen<T> gen){
       return stream(gen).map(s -> s.limit(n).collect(toList()));
   }


   public static <T> Gen<Stream<T>> stream(Gen<T> gen){
       return build(rnd -> Stream.generate(() -> gen.next(rnd)));
   }


   @SafeVarargs
   public static <T> Gen<T> oneOf(T t1, T t2, T... ts){

      List<T> vals = Stream.concat(Stream.of(t1,t2),
                                   Stream.of(ts))
                           .collect(toList());
      return oneOf(vals);
   }

   public static <T> Gen<T> oneOf(Collection<T> ts){
      return build(rnd -> ts.stream()
                            .skip(rnd.nextInt(ts.size()))
                            .findFirst()
                            .get());    
//      return build(rnd -> ts.get(rnd.nextInt(ts.size())));    
   }


   public static <T> Gen<Optional<T>> optional(Gen<T> gen){
      return build(rnd -> Optional.of(rnd.nextBoolean())
                                  .filter(b -> b == true)
                                  .map(b -> gen.next(rnd)));
   }


   public static final Gen<DayOfWeek> DAYOFWEEK = Gen.oneOf(
       DayOfWeek.MONDAY,
       DayOfWeek.TUESDAY,
       DayOfWeek.WEDNESDAY,
       DayOfWeek.THURSDAY,
       DayOfWeek.FRIDAY,
       DayOfWeek.SATURDAY,
       DayOfWeek.SUNDAY
   ); 



}
