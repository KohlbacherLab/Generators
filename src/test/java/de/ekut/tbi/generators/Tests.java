package de.ekut.tbi.generators;


import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import java.time.*;


public final class Tests
{

  private static final Random RND = new Random(42);

  private static final class Foo {

    public final int i;
    public final double d;
    public final String s;

    public Foo(int i, double d, String s){
      this.i = i;
      this.d = d;
      this.s = s;
    }

    @Override
    public String toString(){
      return "Foo(" + i + "," + d + "," + s + ")";
    }
  }
  

  @Test
  public void testIntGen(){

    Gen<Integer> gen = Gen.intsBetween(10,42);

/*
    Prop<Integer> within10to42 = Prop.forAll(i -> i >= 0 && i < 42);
    
    assertTrue(within10to42.check(gen));
*/

    assertTrue(
      Stream.generate(() -> gen.next(RND))
            .limit(25)
            .allMatch(i -> i >= 0 && i < 42)
    );
  }


  @Test
  public void testStringGen(){

    int n = 14;

    Gen<String> alphaNums = Gen.alphaNumeric(n);

    assertTrue(
      Stream.generate(() -> alphaNums.next(RND))
            .limit(50)
            .allMatch(s -> s.length() == n 
                      && s.matches("^[a-zA-Z0-9]+$"))
    );

  }


  @Test
  public void testFiltering(){

    Gen<Integer> evens = Gen.indices()
                            .filter(i -> i%2 == 0);

    assertTrue(Stream.generate(() -> evens.next(RND))
                     .limit(30)
                     .allMatch(i -> i%2 == 0));
  }


  @Test
  public void testOneOfGen(){

    Gen<String> vowels = Gen.oneOf("A","E","I","O","U","Y");
  }


  @Test
  public void testListGen(){

//    Gen<List<Integer>> gen = Gen.listOf(15,Gen.between(10,42));
    Gen<List<Integer>> gen = Gen.collectionOf(List.class,15,Gen.intsBetween(10,42));

    List<Integer> ints = gen.next(RND);

    assertTrue(ints.size() == 15);

    assertTrue(ints.stream()
                   .allMatch(i -> i >= 0 && i < 42) );
  }


  @Test
  public void testMapGen(){

    int n = 10;

    Gen<Map<Integer,String>> maps = Gen.mapOf(n,
                                              Gen.indices(),
                                              Gen.alphaNumeric(15));

    Map<Integer,String> map = maps.next(RND);

    assertTrue(map.size() == n);

  }


  @Test
  public void testFooGen(){


    Gen<Foo> genfoo = Gen.lift(
      Gen.ints(),
      Gen.doubles(),
      Gen.uuidStrings(),
      Foo::new
    );
 
//    Gen<Foo> genfoo = Gen.of(Foo.class);

    Gen<List<Foo>> genfoos = Gen.listOf(15,genfoo);

    List<Foo> foos = genfoos.next(RND);

    assertTrue(foos.size() == 15);

  }


  @Test
  public void testPatientGen(){

    Gen<Patient> patient = Gen.lift(
      Gen.uuidStrings(),
      Gen.oneOf(Patient.Gender.MALE,  Patient.Gender.FEMALE,
                Patient.Gender.OTHER, Patient.Gender.UNKNOWN),
      Gen.localDatesBetween(
            LocalDate.of(1979,1,1), LocalDate.of(1990,1,1)
          ),
      Gen.optional(Gen.localDateNow()),
      Patient::of
    );

/*    
    Gen.register(
      Gen.oneOf(Patient.Gender.MALE,
                Patient.Gender.FEMALE,
                Patient.Gender.OTHER,
                Patient.Gender.UNKNOWN),
      Patient.Gender.class
    );

    Gen<Patient> patient = Gen.of(Patient.class);
*/
 
    Gen<List<Patient>> genpatients = Gen.listOf(15,patient);

    List<Patient> patients = genpatients.next(RND);

    assertTrue(patients.size() == 15);
 
  }


}
