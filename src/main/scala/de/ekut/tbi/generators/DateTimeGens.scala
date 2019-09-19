package de.ekut.tbi.generators




import java.time.{
  DayOfWeek,
  Instant,
  LocalDate,
  LocalDateTime,
  Month
}

import DayOfWeek._
import Month._


object DateTimeGens
{

/*
  def between(
    start: LocalDate,
    end: LocalDate
  ): Gen[LocalDate] = Gen.between(start.toEpochDay,
                                  end.toEpochDay).map(LocalDate.ofEpochDay)
*/

  val localDateNow: Gen[LocalDate] = Gen { () => LocalDate.now }

  val localDateTimeNow: Gen[LocalDateTime] = Gen { () => LocalDateTime.now }

  val instantNow: Gen[Instant] = Gen { () => Instant.now }


  val dayOfWeek: Gen[DayOfWeek]= 
    Gen.oneOf(
      MONDAY,TUESDAY,WEDNESDAY,
      THURSDAY,FRIDAY,SATURDAY,SUNDAY
    )

  val month: Gen[Month] =
    Gen.oneOf(
      JANUARY, FEBRUARY, MARCH,
      APRIL,   MAY,      JUNE,
      JULY,    AUGUST,   SEPTEMBER,
      OCTOBER, NOVEMBER, DECEMBER
    )


  val localDate: Gen[LocalDate] = Gen {
    rnd => LocalDate.of(Gen.int.next(rnd),
                        month.next(rnd),
                        Gen.between(1,28).next(rnd))
  }

  def localDate(
    y: Gen[Int],
    m: Gen[Month],
    d: Gen[Int]
  ): Gen[LocalDate] = Gen {
    rnd => LocalDate.of(y.next(rnd),
                        m.next(rnd),
                        d.next(rnd))
  }


}
