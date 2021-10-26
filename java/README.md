## Java Usage examples (Work In Progress)


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

Gen<Patient> genPat =
  Gen.given(
    Gen.uuidStrings(),                   // Gen of identifier Strings
    Gen.oneOf(Patient.Gender.MALE,       // Gen of values from closed value set: here Gender
              Patient.Gender.FEMALE,
              Patient.Gender.OTHER,
              Patient.Gender.UNKNOWN),
    Gen.localDatesBetween(               // Gen of LocalDate between given start and end
      LocalDate.of(1979,1,1), LocalDate.of(1990,1,1)
    ),
    Gen.optional(Gen.localDateNow())     // Gen of Optional type: here LocalDate
  )
  .map(Patient::of);                   // Function to map respective Gen outputs to: here Patient factory method



// ----------------------------------------------------------------------------
// Automatic derivation of Gen<T> from T constructor or factory method type signature
// ----------------------------------------------------------------------------

Gen<Patient> genPatDerived = Gen.deriveFor(Patient.class);  // Will use default generators for all primitives the type is broken down to

Gen.register(String.class, Gen.letters(42));  // Register the generator of String of 42 letters wherever type String is encountered in subsequent derivation calls

...

Gen<Bar> genBar = // Derive a generator for class Bar...
  Gen.deriveFor(  
    Bar.class,
    Map.of(       // ... using the following generators wherever the key type is encountered
      int.class, Gen.intsBetween(1,42),
      String.class, Gen.constant("bar")
    )
  );



// ----------------------------------------------------------------------------
// Working with Generators
// ----------------------------------------------------------------------------

// Create java.util.Random instance as Generator seed
Random rnd = new Random(42);


Patient pat = genPat.next(rnd)

// Create Generator of Patient-Lists with size 15
Gen<List<Patient>> genPatients = Gen.listOf(15,genPat);

List<Patient> patients = genPatients.next(rnd);


// Usage with Streams
Stream<Patient> patients = Stream.generate(() -> genpat.next(rnd)) //... do something with Stream

```
