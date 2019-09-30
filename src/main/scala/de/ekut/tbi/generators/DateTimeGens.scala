package de.ekut.tbi.generators




import java.time.{
  DayOfWeek,
  Instant,
  LocalDate,
  LocalDateTime,
  LocalTime,
  Month
}

import DayOfWeek._
import Month._


object DateTimeGens
{

  val localDateNow: Gen[LocalDate] = Gen { () => LocalDate.now }

  val localDateTimeNow: Gen[LocalDateTime] = Gen { () => LocalDateTime.now }

  val instantNow: Gen[Instant] = Gen { () => Instant.now }


  def instantsBetween(
    start: Instant,
    end: Instant
  ): Gen[Instant] = Gen.longsBetween(
    start.toEpochMilli,
    end.toEpochMilli
  ).map(Instant.ofEpochMilli)


  def localDatesBetween(
    start: LocalDate,
    end: LocalDate
  ): Gen[LocalDate] = Gen.longsBetween(
    start.toEpochDay,
    end.toEpochDay
  ).map(LocalDate.ofEpochDay)


  def localTimesBetween(
    start: LocalTime,
    end: LocalTime
  ): Gen[LocalTime] = Gen.intsBetween(
    start.toSecondOfDay,
    end.toSecondOfDay
  )
  .map(_.toLong)
  .map(LocalTime.ofSecondOfDay)


  def localDateTimesBetween(
    start: LocalDateTime,
    end: LocalDateTime
  ): Gen[LocalDateTime] = for {
    d <- localDatesBetween(start.toLocalDate,
                           end.toLocalDate)
    t <- localTimesBetween(start.toLocalTime,
                           end.toLocalTime)
  } yield (LocalDateTime.of(d,t))



  val dayOfWeek: Gen[DayOfWeek] = 
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
    rnd => LocalDate.of(
      Gen.ints.next(rnd),
      month.next(rnd),
      Gen.intsBetween(1,28).next(rnd)
    )
  }


  def localDateOf(
    y: Gen[Int],
    m: Gen[Month],
    d: Gen[Int]
  ): Gen[LocalDate] = Gen {
    rnd => LocalDate.of(
      y.next(rnd),
      m.next(rnd),
      d.next(rnd)
    )
  }


}
