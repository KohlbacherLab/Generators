package de.ekut.tbi.generators


import scala.collection.{
  BuildFrom,
  Factory
}


trait VersionSpecifics
{


  type Stream[+T] = LazyList[T]



  def oneOfEach[T,S[X] <: Iterable[X]](
    gens: S[Gen[T]]
  )(
    implicit fac: Factory[T,S[T]]
  ): Gen[S[T]] = Gen {
    rnd => gens.map(_.next(rnd)).to(fac)
  }


  def subsets[T, S[X] <: Iterable[X]](
    ts: S[T]
  )(
    implicit
    fac: Factory[T,S[T]],
    bf: BuildFrom[S[T],T,S[T]]
  ): Gen[S[T]] = {
    Gen {
      rnd => rnd.shuffle(ts)
                .drop(rnd.nextInt(ts.size)).to(fac)
    }
  }


  def lazyList[T](
    gen: Gen[T]
  ): Gen[LazyList[T]] =
    Gen {
      rnd => LazyList.continually(gen.next(rnd))
    }


  def stream[T](
    gen: Gen[T]
  ): Gen[Stream[T]] = lazyList(gen)


  def listOf[T](
    size: Int,
    gen: Gen[T]
  ): Gen[List[T]]


  def iterate[T](
    it: Iterable[T]
  ): Gen[T]


  def iterate[T](
    init: T
  )(
    f: T => T
  ): Gen[T] = iterate(LazyList.iterate(init)(f))


  def indicesFrom(
    i: Int
  ): Gen[Int] = iterate(LazyList.from(i))


  def collectionOf[C[_],T](
    size: Int,
    gen: Gen[T]
  )(
    implicit
    fac: Factory[T, C[T]]
  ): Gen[C[T]] = {
    listOf(size,gen).map(_.to(fac))
  }

  def collection[C[_],T](
    sizes: Gen[Int],
    gen: Gen[T]
  )(
    implicit
    fac: Factory[T, C[T]]
  ): Gen[C[T]] = {
    sizes.flatMap(collectionOf(_,gen))
  }


}
