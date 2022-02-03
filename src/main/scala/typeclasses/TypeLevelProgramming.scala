package typeclasses

// https://www.youtube.com/watch?v=qwUYqv6lKtQ
object TypeLevelProgramming {

  import scala.reflect.runtime.universe._

  // prints the type
  def show[T](value: T)(implicit tag: TypeTag[T]) = tag.toString().replace("typeclasses.TypeLevelProgramming.", "") // for pretty printing

  // type-level programming
  // Peano arithmetic
  trait Naturals

  class _0 extends Naturals

  class Successor[N <: Naturals] extends Naturals // N derives from Naturals

  type _1 = Successor[_0]
  type _2 = Successor[_1] // Successor[Successor[_0]]
  type _3 = Successor[_2] // Successor[Successor[Successor[[_0]]]
  type _4 = Successor[_3]
  type _5 = Successor[_4]

  // _2 < _4?
  trait <[A <: Naturals, B <: Naturals]

  object < {
    implicit def lessThanBasic[B <: Naturals]: <[_0, Successor[B]] = new <[_0, Successor[B]] {} // curly braces because less than is a trait

    implicit def inductive[A <: Naturals, B <: Naturals](implicit lt: A < B): Successor[A] < Successor[B] = new <[Successor[A], Successor[B]] {}

    def apply[A <: Naturals, B <: Naturals](implicit lt: <[A, B]): A < B = lt // finds any A < B the compiler can find
  }

  val comparison: <[_0, _1] = <[_0, _1] // calls apply
  val infixNotation: _0 < _1 = <[_0, _1]
  val another: _1 < _3 = <[_1, _3]
  /*
    <.apply[_1, _3] -> requires implicit <[_1, _3]
    inductive[_1, _3] -> requires implicit <[_0, _2]
    lessThanBasic[_1] -> produces implicit <[0, Successor[_1] == <[_0, _2]
   */

  //  val invalidComparison: _3 < _2 = <[_3, _2] - will not compile, validated by the compiler

  trait <=[A <: Naturals, B <: Naturals]

  object <= {
    implicit def lessThanEqualBasic[B <: Naturals]: <=[_0, B] = new <=[_0, B] {}

    implicit def inductive[A <: Naturals, B <: Naturals](implicit lt: A <= B): Successor[A] <= Successor[B] = new <=[Successor[A], Successor[B]] {}

    def apply[A <: Naturals, B <: Naturals](implicit lte: <=[A, B]): A <= B = lte
  }

  val lteTest: _1 <= _1 = <=[_1, _1] // calls apply
  //  val invalidLte: _3 <= _1 = <=[_3, _1] won't compile - can't build implicit instance

  def main(args: Array[String]): Unit = {
    println(show(List(1, 2, 3)))
    println(show(another))
  }
}
