package de.ekut.tbi.generators


import scala.util.{Either,Random}

import java.util.UUID

import java.time.{LocalDate,
                  LocalDateTime,
                  Instant}

import org.scalatest.FlatSpec



sealed trait Gender
object Gender
{
  final case object Male extends Gender
  final case object Female extends Gender
  final case object Other extends Gender
  final case object Unknown extends Gender
}

object Name
{
  case class Given(name: String)
  case class Family(name: String)
}

case class Name(given: Name.Given,
                family: Name.Family)

case class PatId(value: UUID)
case class Pseudonym(value: UUID)

sealed trait Patient
{
  val id: PatId
  val gender: Gender
  val birthDate: LocalDate
  val dateOfDeath: Option[LocalDate]
  val lastUpdate: Instant
}

case class IdentifiedPatient
(
  id: PatId,
  gender: Gender,
  name: Name, 
  birthDate: LocalDate,
  dateOfDeath: Option[LocalDate],
  lastUpdate: Instant
) extends Patient

case class PseudonymizedPatient
(
  id: PatId,
  gender: Gender,
  pseudonym: Pseudonym, 
  birthDate: LocalDate,
  dateOfDeath: Option[LocalDate],
  lastUpdate: Instant
) extends Patient


object Gens
{

  import Gen._

/*
  implicit val maleGen    = Gen.const(Gender.Male)
  implicit val femaleGen  = Gen.const(Gender.Female)
  implicit val otherGen   = Gen.const(Gender.Other)
  implicit val unknownGen = Gen.const(Gender.Unknown)
  implicit val genderGen: Gen[Gender] = Gen.of[Gender]
*/

  implicit val genderGen: Gen[Gender] =
    Gen.oneOf(Gender.Male,Gender.Female,Gender.Other,Gender.Unknown)

  implicit val givenNameGen: Gen[Name.Given] = 
    Gen.oneOf("Hans","Ute","Peter","Petra")
       .map(Name.Given)

  implicit val familyNameGen: Gen[Name.Family] = 
    Gen.oneOf("Müller","Maier","Schmidt","Mayer")
       .map(Name.Family)

  implicit val idGen      = Gen.uuids.map(PatId)
  implicit val psnGen     = Gen.uuids.map(Pseudonym)
  implicit val dateGen    = DateTimeGens.localDateNow
  implicit val optDateGen = Gen.option(dateGen)
  implicit val instGen    = DateTimeGens.instantNow

  implicit val patGen = Gen.of[Patient]

}


class Tests extends FlatSpec
{

  import Gens._

  implicit val rnd = new Random(42)


  "PositiveInt generation" should "work" in {

     val ints = List.fill(10)(Gen.positiveInts.next)

     assert(ints.forall(_ > 0))
  }


  "Alphanumeric String generation" should "work" in {

     val strs = List.fill(50)(Gen.alphaNumeric(20).next)

     assert(strs.forall(_.matches("^[a-zA-Z0-9]+$")))
  }


  "Gen[(Int,Double)]" should "work" in {
    
     val pairGen = for {
       i <- Gen.ints
       d <- Gen.doubles
     } yield ((i,d))

     val pairs = Gen.collection[Set,(Int,Double)](Gen.between(5,10),pairGen).next
  }


  "Gen[Patient]" should "work" in {

    val pats = List.fill(10)(Gen.of[Patient].next)

    pats foreach println

  }


  "Gen[IdentifiedPatient]" should "be monadically combinable and working" in {

    val names = for {
      g <- Gen.of[Name.Given]
      f <- Gen.of[Name.Family]
    } yield (Name(g,f))

    val patGen = for {
      id     <- Gen.uuids.map(PatId)
      gender <- Gen.of[Gender]
      name   <- names
      bd     <- DateTimeGens.localDateNow
      dod    <- Gen.option(DateTimeGens.localDateNow)
      ts     <- DateTimeGens.instantNow
    } yield (IdentifiedPatient(id,gender,name,bd,dod,ts))

    val pats = List.fill(10)(patGen.next)

  }


}
