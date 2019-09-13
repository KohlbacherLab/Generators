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

object Gens
{

  import Gen._

  implicit val genderGen: Gen[Gender] =
    Gen.oneOf(Gender.Male,Gender.Female,Gender.Other,Gender.Unknown)

  implicit val givenNameGen: Gen[Name.Given] =
    Gen.oneOf("Hans","Ute","Peter","Petra")
       .map(Name.Given)

  implicit val familyNameGen: Gen[Name.Family] =
    Gen.oneOf("Müller","Maier","Schmidt","Mayer")
       .map(Name.Family)

//  implicit val idGen      = Gen.uuid.map(PatId)
  implicit val uuidGen    = Gen.uuid
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

     val ints = List.fill(10)(Gen.positiveInt.next)

     assert(ints.forall(_ > 0))
  }


  "Gen[(Int,Double)]" should "be derivable and working" in {
    
     val pairGen = for {
       i <- Gen.int
       d <- Gen.double
     } yield ((i,d))

     val pairs = Gen.listOf(10,pairGen).next

     pairs foreach println
  }


  "Gen[Patient]" should "be monadically combinable and working" in {

    val names = for {
      g <- Gen.of[Name.Given]
      f <- Gen.of[Name.Family]
    } yield (Name(g,f))


    val patGen = for {
      id     <- Gen.uuid.map(PatId)
      gender <- Gen.of[Gender]
      name   <- names
      bd     <- DateTimeGens.localDateNow
      dod    <- Gen.option(DateTimeGens.localDateNow)
      ts     <- DateTimeGens.instantNow
    } yield (Patient(id,gender,name,bd,dod,ts))


//    val pats = Gen.listOf(10,patGen).next
    val pats = List.fill(10)(patGen.next)

    pats foreach println

  }



}
