package lectures.part2afp

object LazyEvaluation extends App {

  // lazy DELAYS the evaluation of values
  lazy val x: Int = {
    println("hello")
    42
  }

  println(x)
  println(x)
  // prints: hello 42 42

  // examples of implications:

  // side effects
  def sideEffectCondition: Boolean = {
    println("Boo")
    true
  }

  def simpleCondition: Boolean = false

  lazy val lazyCondition = sideEffectCondition
  println(if (simpleCondition && lazyCondition) "yes" else "no")

  // in conjunction with call by name
  def byNameMethod(n: => Int): Int = n + n + n + 1

  def retrieveMagicValue = {
    // side effect or long computation
    println("waiting")
    // Thread.sleep(1000)
    42
  }

  println(byNameMethod(retrieveMagicValue)) // evaluates n 3 times

  // use lazy vals
  def lazyByNameMethod(n: => Int): Int = {
    // Pattern is called: CALL BY NEED
    lazy val t = n // lazy vals only evaluated once
    t + t + t + 1
  }

  println(lazyByNameMethod(retrieveMagicValue)) // evaluates n once, so only one wait

  // filtering with lazy vals
  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }

  def greaterThan20(i: Int): Boolean = {
    println(s"$i is greater than 20?")
    i > 20
  }

  val numbers = List(1, 25, 40, 5, 23)
  val lt30 = numbers.filter(lessThan30) // List(1, 25, 5, 23)
  val gt20 = lt30.filter(greaterThan20)

  println(gt20)

  val lt30lazy = numbers.withFilter(lessThan30) // withFilter uses lazy vals under the hood
  val gt20lazy = lt30lazy.withFilter(greaterThan20)

  println
  println(gt20lazy) // prints scala.collection.TraversableLike$WithFilter@3cda1055
  gt20lazy.foreach(println)

  // for-comprehensions use withFilter with guards
  for {
    a <- List(1, 2, 3) if a % 2 == 0 // uses lazy vals
  } yield a + 1

  // equivalent to
  List(1, 2, 3).withFilter(_ % 2 == 0).map(_ + 1) // List[Int]

  /**
   * Exercise: implement a lazily evaluated, singly linked STREAM of elements
   * Head is accessible, tail are lazily evaluated
   *
   * naturlas = MyStream.from(1)(x => x + 1) = stream of natural numbers (potentially infinite!)
   * naturals.take(100).foreach(println) // lazily evaluated stream of the first 100 naturals (finite stream)
   * naturals.foreach(println) // will crash - infinite!
   * naturals.map(_ * 2) // stream of even numbers (potentially infinite)
   */

  abstract class MyStream[+A] {
    def isEmpty: Boolean
    def head: A
    def tail: MyStream[A]

    def #::[B >: A](element: B): MyStream[B] // prepend operator
    def ++[B >: A](anotherStream: MyStream[B]): MyStream[B] // concatenates two streams

    def foreach(f: A => Unit): Unit
    def map[B](f: A => B): MyStream[B]
    def flatMap[B](f: A => MyStream[B]): MyStream[B]
    def filter(predicate: A => Boolean): MyStream[A]

    def take(n: Int): MyStream[A] // takes the first n elements out of this stream
    def takeAsList(n: Int): List[A]
  }



  object MyStream {
    def from[A](start: A)(generator: A => A): MyStream[A] = ???
  }
}
