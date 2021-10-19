package de.ekut.tbi.generators;


import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;

import java.time.*;


public final class Tests
{

  private static final Random RND = new Random(42);

  private static final class Foo {

    public final int i;
    public final double d;
    public final List<String> s;

    public Foo(
      int i,
      double d,
      List<String> s
    ){
      this.i = i;
      this.d = d;
      this.s = s;
    }

    @Override
    public String toString(){
      return "Foo(" + i + "," + d + "," + s + ")";
    }
  }
  

  private static final class Bar {

    private int i;
    private double d;
    private Map<String,String> s;

    public Bar(){ }

    public Bar setInt(int i){
      this.i = i;
      return this;
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
 


 
  @Test
  public void testIntGen(){

    var start = 10;
    var end   = 42;

    Gen<Integer> gen = Gen.intsBetween(start,end);

    assertTrue(
      Stream.generate(() -> gen.next(RND))
        .limit(250)
        .allMatch(
          i -> i >= start && i < end
        )
    );
  }


  @Test
  public void testStringGen(){

    int n = 14;

    Gen<String> alphaNums = Gen.alphaNumeric(n);

    assertTrue(
      Stream.generate(() -> alphaNums.next(RND))
        .limit(50)
        .allMatch(
          s -> s.length() == n &&
               s.matches("^[a-zA-Z0-9]+$")
        )
    );

  }


  @Test
  public void testFiltering(){

    Gen<Integer> evens = Gen.indices()
                            .filter(i -> i%2 == 0);

    assertTrue(
      Stream.generate(() -> evens.next(RND))
        .limit(30)
        .allMatch(i -> i%2 == 0)
    );
  }


  @Test
  public void testGenDistribution(){

    var pA = 0.25;
    var pE = 0.35;
    var pI = 0.13;
    var pO = 0.11;
    var pU = 0.10;
    var pY = 0.06;

/*
    var vowelGen = Gen.distribution(
      Gen.weighted("A",pA),
      Gen.weighted("E",pE),
      Gen.weighted("I",pI),
      Gen.weighted("O",pO),
      Gen.weighted("U",pU),
      Gen.weighted("Y",pY)
    ); 
*/

    var vowelGen = Gen.distribution(
      Map.of(
        "A",pA,
        "E",pE,
        "I",pI,
        "O",pO,
        "U",pU,
        "Y",pY
      )
    ); 

    var n = 100000;

    var numA = 0;
    var numE = 0;
    var numI = 0;
    var numO = 0;
    var numU = 0;
    var numY = 0;

    var vowels =
      Stream.generate(() -> vowelGen.next(RND))
        .limit(n)
        .collect(toList());

    for (String vowel : vowels){

      switch (vowel){
        case "A": { numA++; break; }
        case "E": { numE++; break; }
        case "I": { numI++; break; }
        case "O": { numO++; break; }
        case "U": { numU++; break; }
        case "Y": { numY++; break; }
        default: { break; }
      }
    }

    var freqA = (double)numA/n;
    var freqE = (double)numE/n;
    var freqI = (double)numI/n;
    var freqO = (double)numO/n;
    var freqU = (double)numU/n;
    var freqY = (double)numY/n;

    assertTrue(freqA > pA-0.05 && freqA < pA+0.05);
    assertTrue(freqE > pE-0.05 && freqE < pE+0.05);
    assertTrue(freqI > pI-0.05 && freqI < pI+0.05);
    assertTrue(freqO > pO-0.05 && freqO < pO+0.05);
    assertTrue(freqU > pU-0.05 && freqU < pU+0.05);
    assertTrue(freqY > pY-0.05 && freqY < pY+0.05);

  }


  @Test
  public void testListGen(){

    Gen<List<Integer>> gen =
      Gen.collectionOf(
        ArrayList::new,
        15,
        Gen.intsBetween(10,42)
      );

    var ints = gen.next(RND);

    assertTrue(ints.size() == 15);

    assertTrue(
      ints.stream()
          .allMatch(i -> i >= 0 && i < 42)
    );
  }


  @Test
  public void testMapGen(){

    int n = 10;

    Gen<Map<Integer,String>> maps =
      Gen.mapOf(
        n,
        Gen.indices(),
        Gen.alphaNumeric(15)
      );

    var map = maps.next(RND);

    assertTrue(map.size() == n);

  }


  @Test
  public void testFooGen(){

    Gen<Foo> genfoo = Gen.given(
      Gen.ints(),
      Gen.doubles(),
      Gen.listOf(5,Gen.uuidStrings())
    )
    .yield(Foo::new);
 
    Gen<List<Foo>> genfoos = Gen.listOf(15,genfoo);

    List<Foo> foos = genfoos.next(RND);

    assertTrue(foos.size() == 15);

  }


  @Test(expected = Test.None.class)
  public void testFooGenDerivation(){

    Gen<Foo> genFoo =
      Gen.deriveFor(Foo.class);

  }


  @Test(expected = Test.None.class)
  public void testBarGenDerivation(){

    Gen<Bar> genBar =
      Gen.deriveFor(
        Bar.class,
        Map.of(
          int.class, Gen.intsBetween(1,42)
        )
      );
 
  }


  @Test(expected = Test.None.class)
  public void testEnumGenDerivation(){

    Gen<Patient.Gender> genderGen =
      Gen.deriveFor(Patient.Gender.class);

  }


  @Test(expected = Test.None.class)
  public void testPatientGenDerivation(){

    Gen<Patient> genPatient =
      Gen.deriveFor(Patient.class);

  }


  @Test
  public void testComposedPatientGen(){

    Gen<Patient> patient =
      Gen.given(
        Gen.uuidStrings(),
        Gen.oneOf(
          Patient.Gender.MALE,
          Patient.Gender.FEMALE,
          Patient.Gender.OTHER,
          Patient.Gender.UNKNOWN
        ),
        Gen.localDatesBetween(
          LocalDate.of(1979,1,1), LocalDate.of(1990,1,1)
        ),
        Gen.optional(Gen.localDateNow())
      )
      .yield(Patient::of);
 
    Gen<List<Patient>> genpatients = Gen.listOf(15,patient);

    List<Patient> patients = genpatients.next(RND);

    assertTrue(patients.size() == 15);
 
  }


}
