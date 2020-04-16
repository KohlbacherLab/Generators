package de.ekut.tbi.generators



import scala.collection.generic.CanBuildFrom


trait VersionSpecifics
{

  def oneOfEach[T,S[X] <: Traversable[X]](
    gens: S[Gen[T]]
  )(
    implicit
    bf: CanBuildFrom[S[T], T, S[T]]
  ): Gen[S[T]] = Gen {
    rnd => gens.map(_.next(rnd)).to[S]
  }


  def subsets[T, S[X] <: Traversable[X]](
    ts: S[T]
  )( 
    implicit
    bf: CanBuildFrom[S[T], T, S[T]]
  ): Gen[S[T]] = {
    Gen { 
      rnd => rnd.shuffle(ts)
                .drop(rnd.nextInt(ts.size)).to[S]
    } 
  }


  def stream[T](
    gen: Gen[T]
  ): Gen[Stream[T]] =
    Gen {
      rnd => Stream.continually(gen.next(rnd))
    }


  def iterate[T](
    it: Iterable[T]
  ): Gen[T]

  def iterate[T](
    init: T
  )(
    f: T => T
  ): Gen[T] = iterate(Stream.iterate(init)(f))


  def indicesFrom(
    i: Int
  ): Gen[Int] = iterate(Stream.from(i))


  def listOf[T](
    size: Int,
    gen: Gen[T]
  ): Gen[List[T]]


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




}
