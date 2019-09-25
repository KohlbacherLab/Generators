# Scala/Java Generators


## Purpose / Status

Provide type-class-based utilities for generation of arbitrary data types
with support for automatic derivation of type class instances.


At present more of a learning project largely inspired from exercises
in ["Functional Programming in Scala"](https://www.manning.com/books/functional-programming-in-scala)
and [ScalaCheck](https://www.scalacheck.org/)


## Usage examples (Work In Progress)

### Scala example:

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

### Java example:


```java
// ----------------------------------------------------------------------------
// Declare simple class
// ----------------------------------------------------------------------------
public final class Patient
{

  public enum Gender {
    MALE,FEMALE,OTHER,UNKNOWN
  }

  private final String id;
  private final Gender gender;
  private final LocalDate birthDate;
  private final Optional<LocalDate> dateOfDeath;

  private Patient(
    String id,
    Gender gender,
    LocalDate birthDate,
    Optional<LocalDate> dateOfDeath
  ){
    this.id = id;
    this.gender = gender;
    this.birthDate = birthDate;
    this.dateOfDeath = dateOfDeath;
  }

  public static Patient of(
    String id,
    Gender gender,
    LocalDate birthDate,
    Optional<LocalDate> dateOfDeath
  ){
    return new Patient(id,gender,birthDate,dateOfDeath);
  }

}

// ----------------------------------------------------------------------------
// Create Gen<Patient> by combining Gens for respective
// Patient properties
// (syntax analogous to Scala "for comprehensions"):
// ----------------------------------------------------------------------------

Gen<Patient> genpat = Gen.lift(
  Gen.uuidStrings(),                   // Gen of identifier Strings
  Gen.oneOf(Patient.Gender.MALE,       // Gen of values from closed value set: here Gender
            Patient.Gender.FEMALE,
            Patient.Gender.OTHER,
            Patient.Gender.UNKNOWN),
  Gen.localDatesBetween(LocalDate.of(1979,1,1),  // Gen of LocalDate between given start and end
                        LocalDate.of(1990,1,1)),
  Gen.optional(Gen.localDateNow()),    // Gen of Optional type: here LocalDate
  Patient::of                          // Function to map respective Gen outputs to: here Patient factory method
);


// WORK IN PROGRESS:
// Automatic derivation of Gen<T> from T constructor or factory method type signature


// ----------------------------------------------------------------------------
// Working with Generators
// ----------------------------------------------------------------------------

// Create java.util.Random instance as Generator seed
Random rnd = new Random(42);


Patient pat = genpat.next(rnd)

// Create Generator of Patient-Lists with size 15
Gen<List<Patient>> genpatients = Gen.listOf(15,genpat);

List<Patient> patients = genpatients.next(rnd);


// Usage with Streams
Stream<Patient> patients = Stream.generate(() -> genpat.next(rnd)) //... do something with Stream

```
