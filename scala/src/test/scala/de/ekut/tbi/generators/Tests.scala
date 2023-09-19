package de.ekut.tbi.generators


import java.util.UUID
import java.time.{
  LocalDate,
  LocalDateTime,
  Instant
}
import scala.util.{
  Either,
  Random
}
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

case class Name(
  `given`: Name.Given,
  family: Name.Family
)

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


  implicit val genderGen: Gen[Gender] =
    Gen.oneOf(Gender.Male,Gender.Female,Gender.Other,Gender.Unknown)

  implicit val givenNameGen: Gen[Name.Given] = 
    Gen.oneOf("Hans","Ute","Peter","Petra")
       .map(Name.Given)

  implicit val familyNameGen: Gen[Name.Family] = 
    Gen.oneOf("Müller","Maier","Schmidt","Mayer")
       .map(Name.Family)

  implicit val idGen: Gen[PatId] =
    Gen.uuids.map(PatId)

  implicit val psnGen: Gen[Pseudonym] =
    Gen.uuids.map(Pseudonym)

  implicit val dateGen: Gen[LocalDate] =
    DateTimeGens.localDateNow

  implicit val optDateGen: Gen[Option[LocalDate]] =
    Gen.option(dateGen)

  implicit val instGen: Gen[Instant] =
    DateTimeGens.instantNow

//  implicit val patGen: Gen[Patient] =
//    Gen.of[Patient]

}


class Tests extends FlatSpec
{

  import Gens._

  implicit val rnd: Random = new Random(42)


  "PositiveInt generation" should "work" in {

     val ints = List.fill(10)(Gen.positiveInts.next)

     assert(ints.forall(_ > 0))
  }


  "Alphanumeric String generation" should "work" in {

     val strs = List.fill(50)(Gen.alphaNumeric(20).next)

     assert(strs.forall(_.matches("^[a-zA-Z0-9]+$")))
  }


  "Int generation within interval" should "work" in {

    val start = 1000
    val end = 10000

    val intsInInterval = Gen.intsBetween(start,end)

    assert(
      List.fill(1000)(intsInInterval.next)
        .forall(l => l >= start && l < end)
    )

  }

  "Long generation within interval" should "work" in {

    val start: Long = 1000L
    val end: Long = 10000L

    val longsInInterval = Gen.longsBetween(start,end)

    assert(
      List.fill(1000)(longsInInterval.next)
        .forall(l => l >= start && l < end)
    )

  }


  "Subset generation" should "work" in {

     val nums = Set(0,1,2,3,4,5,6,7,8,9)

     val genNums = Gen.subsets(nums)

     assert(
       List.fill(500)(genNums.next)
         .forall( _.forall(nums.contains(_)))
     )

  }


  "LocalDate generation within interval" should "work" in {

    val start = LocalDate.of(1980,1,1)
    val end   = LocalDate.of(1990,1,1)

    val datesInInterval = DateTimeGens.localDatesBetween(start,end)

    assert(
      List.fill(500)(datesInInterval.next)
            .forall(d => (d.isAfter(start) || d.isEqual(start))
                         && (d.isBefore(end) || d.isEqual(end)))
    )

  }


  "Generator of weighted distribution" should "work" in {

    val weightedGenders = Gen.distribution(
      (5,Gender.Male),
      (5,Gender.Female),
      (2,Gender.Other),
      (1,Gender.Unknown)
    )

    val n = 100000

    val genders = List.fill(n)(weightedGenders.next)

    val (nm,nf,no,nu) = genders.foldLeft((0,0,0,0))((acc,g) => g match {
      case Gender.Male    => (acc._1+1,acc._2,acc._3,acc._4)
      case Gender.Female  => (acc._1,acc._2+1,acc._3,acc._4)
      case Gender.Other   => (acc._1,acc._2,acc._3+1,acc._4)
      case Gender.Unknown => (acc._1,acc._2,acc._3,acc._4+1)
    })

  }


  "Gen[(Int,Double)]" should "work" in {
    
     val pairGen = for {
       i <- Gen.ints
       d <- Gen.doubles
     } yield ((i,d))

     val pairs = Gen.collection[Set,(Int,Double)](Gen.intsBetween(5,10),pairGen).next
  }



  "Gen[Patient]" should "work" in {

    val pats =
      List.fill(30)(Gen.of[Patient].next)

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
