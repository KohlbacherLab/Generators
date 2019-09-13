package de.ekut.tbi.generators


import java.util.UUID

import scala.util.{
  Either, Random
}

import cats.data.{
  NonEmptyList
}

import shapeless.{
  HList, HNil, ::, Generic, Lazy
}



trait Gen[T]
{

  def next(implicit rnd: Random): T

  def map[U](
    f: T => U
  ): Gen[U] = Gen(rnd => f(this.next(rnd)))

  def flatMap[U](
    f: T => Gen[U]
  ): Gen[U] = Gen(rnd => f(this.next(rnd)).next(rnd)) 

}



object Gen
{

  def const[T](t: => T): Gen[T] = apply { rnd => t }

  val int: Gen[Int] = apply { rnd => rnd.nextInt }

  val long: Gen[Long] = apply { rnd => rnd.nextLong }

  val double: Gen[Double] = apply { rnd => rnd.nextDouble }

  val gaussian: Gen[Double] = apply { rnd => rnd.nextGaussian }

  val float: Gen[Float] = apply { rnd => rnd.nextFloat }
  
  val boolean: Gen[Boolean] = apply { rnd => Random.nextBoolean }

  val char: Gen[Char] = apply { rnd => rnd.nextPrintableChar }
  
  val uuid: Gen[UUID] = apply { rnd => UUID.randomUUID }

  val identifier: Gen[String] = uuid.map(_.toString)


  def option[T](
    gen: Gen[T]
  ): Gen[Option[T]] = apply {
    rnd => Option(rnd.nextBoolean)
                 .filter(b => b)
                 .map(_ => gen.next(rnd))
  }

  def either[T,U](
    tGen: Gen[T],
    uGen: Gen[U]
  ): Gen[Either[T,U]] = apply {
    rnd => Either.cond(rnd.nextBoolean,
                       uGen.next(rnd),
                       tGen.next(rnd))
  }
     
     
  implicit val hnil: Gen[HNil] = const(HNil)

  implicit def hlist[H, T <: HList](
    implicit
    head: Lazy[Gen[H]],
    tail: Gen[T]
  ): Gen[H :: T] = apply {
    rnd => head.value.next(rnd) :: tail.next(rnd)
  }


  implicit def generic[T <: Product,R](
    implicit
    gen: Generic.Aux[T,R],
    g: Lazy[Gen[R]]
  ): Gen[T] = apply {
    rnd => gen.from(g.value.next(rnd))
  }
  

  /*
   *  Summoner of Gen[T] derived from generic representation
   *  unless already in implicit scope
   */
  def of[T](implicit gen: Gen[T]): Gen[T] = gen


  def apply[T](f: Random => T): Gen[T] = new Gen[T]{
    def next(implicit rnd: Random): T = f(rnd)
  }

  def between(start: Int, endExcl: Int): Gen[Int] = apply(
    rnd => rnd.nextInt(endExcl-start) + start
  )

  def between(start: Float, end: Float): Gen[Float] = apply(
    rnd => rnd.nextFloat*end + start
  )

  def between(start: Double, end: Double): Gen[Double] = apply(
    rnd => rnd.nextDouble*end + start
  )


  def oneOf[T](
    ts: Seq[T]
  ): Gen[T] = apply(
    rnd => ts.iterator.drop(rnd.nextInt(ts.size)).next
  )

  def oneOf[T](
    t0: T, t1: T, ts: T*
  ): Gen[T] = oneOf(t0 +: t1 +: ts)


  def infiniteStream[T](
    gen: Gen[T]
  ): Gen[Stream[T]] = apply(
    rnd => Stream.continually(gen.next(rnd))
  )  
    
  def listOf[T](
    size: Int,
    gen: Gen[T]
  ): Gen[List[T]] = apply(
    rnd => List.fill(size)(gen.next(rnd))
  )

  def list[T](
    gen: Gen[T]
  ): Gen[List[T]] = apply {
    rnd => listOf(between(42,100).next(rnd),gen).next(rnd)
  } 
  

  def nonEmptyListOf[T](
    size: Int,
    gen: Gen[T]
  ): Gen[NonEmptyList[T]] = apply(
    rnd => NonEmptyList.fromListUnsafe(listOf(size,gen).next(rnd))
  )

  def nonEmptyList[T](
    gen: Gen[T]
  ): Gen[NonEmptyList[T]] = apply {
    rnd => nonEmptyListOf(between(42,100).next(rnd),gen).next(rnd)
  } 


  def positiveInt: Gen[Int] = apply { rnd =>
    val i = rnd.nextInt
    if (i > 0) i
    else -i
  }


}
