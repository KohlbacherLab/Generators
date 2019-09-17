package de.ekut.tbi.generators;


import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.List;
import java.util.stream.Stream;

import java.time.DayOfWeek;


public final class Tests
{

    private static final Random RND = new Random(42);

    private static final class Foo {

       public final int i;
       public final String s;

       public Foo(int i, String s){
           this.i = i;
           this.s = s;
       }

       @Override
       public String toString(){
          return "Foo(" + i + "," + s + ")";
       }
    }
   

    @Test
    public void testIntGen(){

        Gen<Integer> gen = Gen.between(10,42);

        assertTrue(Stream.generate(() -> gen.next(RND))
                         .limit(25)
                         .allMatch(i -> i >= 0 && i < 42) );
    }


    @Test
    public void testListGen(){

        Gen<List<Integer>> gen = Gen.listOf(15,Gen.between(10,42));

        List<Integer> ints = gen.next(RND);

        assertTrue(ints.size() == 15);

        assertTrue(ints.stream()
                       .allMatch(i -> i >= 0 && i < 42) );
    }

    @Test
    public void testFooGen(){

        Gen<Foo> genfoo = Gen.INT.flatMap(
            i -> Gen.IDENTIFIER.map(
            s -> new Foo(i,s))
        );

        Gen<List<Foo>> genfoos = Gen.listOf(15,genfoo);

        List<Foo> foos = genfoos.next(RND);

        assertTrue(foos.size() == 15);

        foos.forEach(System.out::println);
 
    }


}
