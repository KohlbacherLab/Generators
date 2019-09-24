package de.ekut.tbi.generators


import java.util.UUID

import scala.util.{
  Either, Random
}
import scala.collection.generic.CanBuildFrom

import cats.data.{
  NonEmptyList
}

import shapeless.{
  HList, HNil, ::, Generic, Lazy,
  Coproduct, CNil, :+:, Inl, Inr
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

  val ints: Gen[Int] = Gen { rnd => rnd.nextInt }

  val longs: Gen[Long] = Gen { rnd => rnd.nextLong }

  val doubles: Gen[Double] = Gen { rnd => rnd.nextDouble }

  val gaussians: Gen[Double] = Gen { rnd => rnd.nextGaussian }

  val floats: Gen[Float] = Gen { rnd => rnd.nextFloat }
  
  val booleans: Gen[Boolean] = Gen { rnd => Random.nextBoolean }

  val chars: Gen[Char] = Gen { rnd => rnd.nextPrintableChar }
  
  val uuids: Gen[UUID] = Gen { () => UUID.randomUUID }

  val uuidStrings: Gen[String] = uuids.map(_.toString)


  private val alphabet = List(
    "a","b","c","d","e", "A","B","C","D","E",
    "f","g","h","i","j", "F","G","H","I","J",
    "k","l","m","n","o", "K","L","M","N","O",
    "p","q","r","s","t", "P","Q","R","S","T",
    "u","v","w","x","y", "U","V","W","X","Y",
    "z",                 "Z"
  )

  private val digits = List(
    "0","1","2","3","4","5","6","7","8","9"
  )

  def letters(n: Int): Gen[String] =
    stream(oneOf(alphabet))
          .map(s => s.take(n)
                     .foldLeft(new String)(_ + _))
  
  def numeric(n: Int): Gen[String] =
    stream(oneOf(digits))
          .map(s => s.take(n)
                     .foldLeft(new String)(_ + _))
  
  def alphaNumeric(n: Int): Gen[String] =
    stream(oneOf(alphabet ++ digits))
          .map(s => s.take(n)
                     .foldLeft(new String)(_ + _))
  

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


  implicit val cnilGen: Gen[CNil] = Gen { rnd => throw new RuntimeException("Never reached") }

  implicit def coproductGen[H, T <: Coproduct](
    implicit
    head: Lazy[Gen[H]],
    tail: Gen[T]
  ): Gen[H :+: T] = Gen {
    rnd => if (rnd.nextBoolean) Inl(head.value.next(rnd))
           else                 Inr(tail.next(rnd))
  }

  implicit def coproductGenTerminal[H](
    implicit
    head: Lazy[Gen[H]]
  ): Gen[H :+: CNil] = Gen {
    rnd => Inl(head.value.next(rnd))
  }


  implicit def genericGen[T,R](
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


  def indicesFrom(
    i: Int
  ): Gen[Int] = iterate(Stream.from(i)) 

  def indices: Gen[Int] = indicesFrom(0)


  def oneOf[T](
    ts: Seq[T]
  ): Gen[T] = Gen {
    rnd => ts.iterator.drop(rnd.nextInt(ts.size)).next
  }

  def oneOf[T](
    t0: T, t1: T, ts: T*
  ): Gen[T] = oneOf(t0 +: t1 +: ts)


  def stream[T](
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
  ): Gen[List[T]] = {
    sizes.flatMap(listOf(_,gen))
  } 
  
  def mapOf[K,V](
    size: Int,
    keys: Gen[K],
    values: Gen[V]
  ): Gen[Map[K,V]] = Gen {
    rnd => Stream.fill(size)((keys.next(rnd),values.next(rnd))).toMap
  }

  def map[K,V](
    sizes: Gen[Int],
    keys: Gen[K],
    values: Gen[V]
  ): Gen[Map[K,V]] = {
    sizes.flatMap(mapOf(_,keys,values))
  }

  def collectionOf[C[_],T](
    size: Int,
    gen: Gen[T]
  )(
    implicit
    cbf: CanBuildFrom[Nothing, T, C[T]]
  ): Gen[C[T]] = {
    listOf(size,gen).map(_.to[C])
  }

  def collection[C[_],T](
    sizes: Gen[Int],
    gen: Gen[T]
  )(
    implicit
    cbf: CanBuildFrom[Nothing, T, C[T]]
  ): Gen[C[T]] = {
    sizes.flatMap(collectionOf(_,gen))
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
  ): Gen[NonEmptyList[T]] = {
    sizes.flatMap(nonEmptyListOf(_,gen))
  } 


//  def positiveInts: Gen[Int] = ints.filter(_ > 0)
  def positiveInts: Gen[Int] = Gen { rnd =>
    val i = rnd.nextInt
    if (i > 0) i
    else -i
  }


}
