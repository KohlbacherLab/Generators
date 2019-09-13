#Generators


### Purpose / Status

Provide type-class-based utilities for generation of arbitrary data types
with support for automatic derivation of type class instances.


At present more of a learning project largely inspired from exercises
in ["Functional Programming in Scala"](https://www.manning.com/books/functional-programming-in-scala)
and [ScalaCheck](https://www.scalacheck.org/)


### Usage example (Work In Progress)

Simple example:

```scala

import de.ekut.tbi.generators.{Gen,DateTimeGens}


//Define a few model objects

sealed trait Gender
object Gender
{
  case object Male extends Gender
  case object Female extends Gender
  case object Other extends Gender
  case object Unknown extends Gender

}

object Name
{
  case class Given(name: String)
  case class Family(name: String)
}

case class Name(given: Name.Given,
                family: Name.Family)

case class PatId(value: UUID)

case class Patient(id: PatId,
                   gender: Gender,
                   name: Name,
                   birthDate: LocalDate,
                   dateOfDeath: Option[LocalDate],
                   lastUpdate: Instant)


  // Define Gens for custom types

  import Gen.\_

  implicit val genderGen: Gen[Gender] =
    Gen.oneOf(Gender.Male,Gender.Female,Gender.Other,Gender.Unknown)

  implicit val givenNameGen: Gen[Name.Given] =
    Gen.oneOf("Hans","Ute","Peter","Petra")
       .map(Name.Given)

  implicit val familyNameGen: Gen[Name.Family] =
    Gen.oneOf("Müller","Maier","Schmidt","Mayer")
       .map(Name.Family)

  ...


  //---------------------------------------------------------------------------
  // Usage 1: Create Gen[Patient] using for-comprehension
  //---------------------------------------------------------------------------

  val names: Gen[Name] = for {
    g <- Gen.of[Name.Given]
    f <- Gen.of[Name.Family]
  } yield (Name(g,f))


  val patGen: Gen[Patient] = for { 
    id     <- Gen.uuid.map(PatId)
    gender <- Gen.of[Gender]
    name   <- names
    bd     <- DateTimeGens.localDateNow
    dod    <- Gen.option(DateTimeGens.localDateNow)
    ts     <- DateTimeGens.instantNow
  } yield (Patient(id,gender,name,bd,dod,ts))


  //---------------------------------------------------------------------------
  // Usage 2: Compile-time automatic derivation of Gen[Patient]
  //---------------------------------------------------------------------------

  // Bring required Gen instances into scope
  implicit val idGen:      Gen[PatId]             = Gen.uuid.map(PatId)
  implicit val dateGen:    Gen[LocalDate]         = DateTimeGens.localDateNow
  implicit val optDateGen: Gen[Option[LocalDate]] = Gen.option(dateGen)
  implicit val instGen:    Gen[Instant]           = DateTimeGens.instantNow


  implicit val patGen: Gen[Patient] = Gen.of[Patient]



  // Bring rnd generation "seed" into scope
  implicit val rnd = new Random(42)


  val patients = List.fill(10)(patGen.next)



```

