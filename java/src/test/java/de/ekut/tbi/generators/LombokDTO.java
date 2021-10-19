package de.ekut.tbi.generators;


import lombok.*;

import java.time.LocalDate;
import java.time.Instant;


public final class LombokDTO
{

  @Data
  @AllArgsConstructor
  public static final class Foo
  {

    final int num;

    final double dbl;
 
    final String str;

    final LocalDate date;

    final Instant instant;

  }



}

