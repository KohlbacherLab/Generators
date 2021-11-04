package de.ekut.tbi.generators;


import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.groupingBy;

import java.time.*;


public final class Tests
{

  private static final Random RND = new Random(42);

  private static final int N = 100;

 
  @Test
  public void testIntGen(){

    var start = 10;
    var end   = 42;

    Gen<Integer> gen = Gen.intsBetween(start,end);

    assertTrue(
      Stream.generate(() -> gen.next(RND))
        .limit(N)
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
        .limit(N)
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
        .limit(N)
        .allMatch(i -> i%2 == 0)
    );
  }


  @Test
  public void testGenDistribution(){

    Map<String,Double> vowelPs =
      Map.of(
        "A", 0.25,
        "E", 0.35,
        "I", 0.13,
        "O", 0.11,
        "U", 0.10,
        "Y", 0.06
      );

    var vowelGen =
      Gen.distribution(vowelPs); 

    var n = 100000;

    var vowelNs =
      Stream.generate(() -> vowelGen.next(RND))
        .limit(n)
        .collect(groupingBy(v -> v));

    var delta = 0.05;

    assertTrue(
      vowelNs.entrySet()
        .stream()
        .allMatch(
          entry -> {
            var v = entry.getKey();
            var m = entry.getValue().size();
            var p = vowelPs.get(v);

            var freq = (double)m/n;

            return freq > p-delta && freq < p+delta;
          }
        )
    );

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

    Gen<Foo> genfoo =
      Gen.given(
        Gen.ints(),
        Gen.doubles(),
        Gen.enumValues(Foo.Type.class),
        Gen.listOf(5,Gen.uuidStrings())
      )
      .map(Foo::new);
 
    Gen<List<Foo>> genfoos = Gen.listOf(15,genfoo);

    List<Foo> foos = genfoos.next(RND);

    assertTrue(foos.size() == 15);

  }


  @Test
  public void testComposedPatientGen(){

    Gen<Patient> patient =
      Gen.given(
        Gen.uuidStrings(),
        Gen.enumValues(Patient.Gender.class),
        Gen.localDatesBetween(
          LocalDate.of(1979,1,1), LocalDate.of(1990,1,1)
        ),
        Gen.optional(Gen.localDateNow())
      )
      .map(Patient::of);
 
    Gen<List<Patient>> genpatients = Gen.listOf(15,patient);

    List<Patient> patients = genpatients.next(RND);

    assertTrue(patients.size() == 15);
 
  }



  @Test(expected = Test.None.class)
  public void testFooGenDerivation(){

    Gen<Foo> genFoo =
      Gen.deriveFor(Foo.class);

  }


  @Test(expected = Test.None.class)
  public void testBarGenDerivation(){

    var start = 1;
    var end   = 42;

    Gen<Bar> genBar =
      Gen.deriveFor(
        Bar.class,
        Map.of(
          int.class, Gen.intsBetween(start,end)
        )
      );

    assertTrue(
      Stream.generate(() -> genBar.next(RND))
        .limit(N)
        .allMatch(
          bar -> bar.getInt() >= start && bar.getInt() < end
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


  @Test(expected = Test.None.class)
  public void testLombokDTOGenDerivation(){

    Gen<LombokDTO.Foo> genFoo =
      Gen.deriveFor(LombokDTO.Foo.class);

    Gen<LombokDTO.Patient> genPatient =
      Gen.deriveFor(LombokDTO.Patient.class);

  }

}
