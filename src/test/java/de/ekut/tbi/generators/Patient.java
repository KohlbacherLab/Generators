package de.ekut.tbi.generators;



import java.time.LocalDate;
import java.util.Optional;
//import java.util.U;



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
