package de.ekut.tbi.generators;


import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;


import org.hl7.fhir.r4.model.*;



public final class TestHapiFhir
{

  private static final Random RND = new Random(42);


  private static final Gen<Identifier> IDENTIFIERS =
    Gen.uuidStrings()
      .map(s -> new Identifier().setValue(s));      


  @Test
  public void testPatientGen(){

    var genPat =
      Gen.given(
        IDENTIFIERS,
        Gen.enumValues(Enumerations.AdministrativeGender.class)
      )
      .yield(
        (identifier,gender) ->
          new org.hl7.fhir.r4.model.Patient()
                .addIdentifier(identifier)
                .setGender(gender)
      );

  }



}
