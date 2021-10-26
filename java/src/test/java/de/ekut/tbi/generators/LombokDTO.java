package de.ekut.tbi.generators;


import lombok.*;

import java.time.LocalDate;
import java.time.Instant;

import java.util.Optional;
import java.util.UUID;



public final class LombokDTO
{

  @Data
  @AllArgsConstructor
  public static final class Foo
  {

    public enum Type {
      ONE,TWO,THREE,FOUR
    }

    private int num;

    private double dbl;
 
    private String str;

    private LocalDate date;

    private Instant instant;

    private Type type;
  }


  @Data
  @AllArgsConstructor
  public static final class Patient
  {

    public enum Gender {
      MALE,FEMALE,OTHER,UNKNOWN
    }

    private final UUID id;

    private final Gender gender;

    private final LocalDate birthDate;

    private final Optional<LocalDate> dateOfDeath;

  }


}

