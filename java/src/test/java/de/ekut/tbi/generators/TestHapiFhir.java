package de.ekut.tbi.generators;


import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import java.util.Date;
import java.time.LocalDate;
import java.time.Instant;
import static java.time.temporal.ChronoUnit.DAYS;

import org.hl7.fhir.r4.model.*;

import static de.ekut.tbi.generators.Gen.given;


public final class TestHapiFhir
{

  private static final Random RND = new Random(42);


  private static final Gen<Identifier> IDENTIFIERS =
    Gen.uuidStrings()
      .map(s -> new Identifier().setValue(s));      


  private static final Gen<Date> BIRTHDATES =
    Gen.instantsBetween(
      Instant.now().minus(80*12*365,DAYS),  // MUST use DAYS here because YEARS or MONTHS not supported by Instant.minus(long,TemporalUnit)
      Instant.now().minus(42*12*365,DAYS)
    )
    .map(Date::from);


  private static final Gen<HumanName> HUMAN_NAMES =
    given(
      Gen.oneOf("Ute","Sabine","Hans","Peter"),
      Gen.oneOf("Mustermensch","Maier","Müller")
    )
    .map(
      (given,family) ->
        new HumanName().addGiven(given).setFamily(family)
    );


  private static final Gen<Address> ADDRESSES =
    given(
      Gen.oneOf("Musterstr. 42","Haumichblau Weg 24"),
      Gen.intsBetween(70000,80000).map(i -> Integer.toString(i)),
      Gen.oneOf("Musterhausen","Entenhausen","Irgendingen")
    )
    .map(
      (str,plz,city) ->
        new Address().addLine(str).setPostalCode(plz).setCity(city)
    );


  private static final Gen<org.hl7.fhir.r4.model.Patient> PATIENTS =
    given(
      IDENTIFIERS,
      BIRTHDATES,
      HUMAN_NAMES,
      Gen.enumValues(Enumerations.AdministrativeGender.class),
      ADDRESSES 
    )
    .map(
      (identifier,birthdate,name,gender,address) ->
        new org.hl7.fhir.r4.model.Patient()
              .addIdentifier(identifier)
              .setBirthDate(birthdate)
              .addName(name)
              .setGender(gender)
              .addAddress(address)
    );


  @Test
  public void testPatientGen(){

    assertTrue(
      Stream.generate(() -> PATIENTS.next(RND))
        .limit(42)
        .allMatch(
          p ->
            p.hasIdentifier() &&
            p.hasBirthDate() &&
            p.hasName() &&
            p.hasGender() &&
            p.hasAddress()
        )
    );

  }



}
