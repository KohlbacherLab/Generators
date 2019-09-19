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



sealed trait Gen[T]
{

  def next(implicit rnd: Random): T
/*
  def filter(p: T => Boolean): Gen[T] = {
    Gen { rnd => Gen.doFilter(this,p)(rnd) }
  }
*/
  def map[U](
    f: T => U
  ): Gen[U] = Gen(rnd => f(this.next(rnd)))

  def flatMap[U](
    f: T => Gen[U]
  ): Gen[U] = Gen(rnd => f(this.next(rnd)).next(rnd)) 

}



object Gen
{

  @annotation.tailrec
  protected def doFilter[T](
    gen: Gen[T],
    p: T => Boolean
  )(
    rnd: Random
  ): T = {
    val t = gen.next(rnd)
    if (p(t)) t
    else doFilter(gen,p)(rnd)
  }


  def const[T](t: => T): Gen[T] = Gen { () => t }

  val int: Gen[Int] = Gen { rnd => rnd.nextInt }

  val long: Gen[Long] = Gen { rnd => rnd.nextLong }

  val double: Gen[Double] = Gen { rnd => rnd.nextDouble }

  val gaussian: Gen[Double] = Gen { rnd => rnd.nextGaussian }

  val float: Gen[Float] = Gen { rnd => rnd.nextFloat }
  
  val boolean: Gen[Boolean] = Gen { rnd => Random.nextBoolean }

  val char: Gen[Char] = Gen { rnd => rnd.nextPrintableChar }
  
  val uuid: Gen[UUID] = Gen { () => UUID.randomUUID }

  val identifier: Gen[String] = uuid.map(_.toString)


  def option[T](
    gen: Gen[T]
  ): Gen[Option[T]] = Gen {
    rnd => Option(rnd.nextBoolean)
                 .filter(b => b)
                 .map(_ => gen.next(rnd))
  }

  def either[T,U](
    tGen: Gen[T],
    uGen: Gen[U]
  ): Gen[Either[T,U]] = Gen {
    rnd => Either.cond(rnd.nextBoolean,
                       uGen.next(rnd),
                       tGen.next(rnd))
  }
     
     
  implicit val hnil: Gen[HNil] = const(HNil)

  implicit def hlist[H, T <: HList](
    implicit
    head: Lazy[Gen[H]],
    tail: Gen[T]
  ): Gen[H :: T] = Gen {
    rnd => head.value.next(rnd) :: tail.next(rnd)
  }


  implicit def generic[T <: Product,R](
    implicit
    gen: Generic.Aux[T,R],
    g: Lazy[Gen[R]]
  ): Gen[T] = Gen {
    rnd => gen.from(g.value.next(rnd))
  }
  

  /*
   *  Summoner of Gen[T] derived from generic representation
   *  unless already in implicit scope
   */
  def of[T](implicit gen: Gen[T]): Gen[T] = gen


  def apply[T](f: () => T): Gen[T] = new Gen[T]{
    def next(implicit rnd: Random): T = f()
  }

  def apply[T](f: Random => T): Gen[T] = new Gen[T]{
    def next(implicit rnd: Random): T = f(rnd)
  }


  def between(start: Int, endExcl: Int): Gen[Int] = Gen { 
    rnd => rnd.nextInt(endExcl-start) + start
  }

/*
  TODO
  def between(start: Long, endExcl: Long): Gen[Long] = {
    long.filter(l => l >= start && l < endExcl)
  }
*/

  def between(start: Float, end: Float): Gen[Float] = Gen(
    rnd => rnd.nextFloat*(end-start) + start
  )

  def between(start: Double, end: Double): Gen[Double] = Gen(
    rnd => rnd.nextDouble*(end-start) + start
  )


  def iterate[T](
    it: Iterable[T]
  ): Gen[T] = iterate(it.iterator)


  def iterate[T](
    it: Iterator[T]
  ): Gen[T] = Gen { () => it.next }


  def iterate[T](
    init: T
  )(
    f: T => T
  ): Gen[T] = iterate(Stream.iterate(init)(f))


  def indices(
    i: Int
  ): Gen[Int] = iterate(Stream.from(i)) 

  def indices: Gen[Int] = indices(0)


  def oneOf[T](
    ts: Seq[T]
  ): Gen[T] = Gen {
    rnd => ts.iterator.drop(rnd.nextInt(ts.size)).next
  }

  def oneOf[T](
    t0: T, t1: T, ts: T*
  ): Gen[T] = oneOf(t0 +: t1 +: ts)


  def infiniteStream[T](
    gen: Gen[T]
  ): Gen[Stream[T]] = Gen {
    rnd => Stream.continually(gen.next(rnd))
  }
    
  def listOf[T](
    size: Int,
    gen: Gen[T]
  ): Gen[List[T]] = Gen {
    rnd => List.fill(size)(gen.next(rnd))
  }

  def list[T](
    sizes: Gen[Int],
    gen: Gen[T]
  ): Gen[List[T]] = Gen {
    rnd => List.fill(sizes.next(rnd))(gen.next(rnd))
  } 
  

  def nonEmptyListOf[T](
    size: Int,
    gen: Gen[T]
  ): Gen[NonEmptyList[T]] = Gen {
    rnd => NonEmptyList.fromListUnsafe(listOf(size,gen).next(rnd))
  }


  def nonEmptyList[T](
    sizes: Gen[Int],
    gen: Gen[T]
  ): Gen[NonEmptyList[T]] = Gen {
    rnd => nonEmptyListOf(sizes.next(rnd),gen).next(rnd)
  } 


  def positiveInt: Gen[Int] = Gen { rnd =>
    val i = rnd.nextInt
    if (i > 0) i
    else -i
  }


}
