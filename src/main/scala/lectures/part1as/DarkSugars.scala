package lectures.part1as

import scala.util.Try

object DarkSugars extends App {

  // syntax sugar #1: methods with single param
  def singleArgMethod(arg: Int): String = s"$arg little ducks..."

  val description = singleArgMethod {
    // write some complex code...
    42
  }

  val aTryInstance = Try { // reminiscent java's try {
    throw new RuntimeException
  }

  List(1, 2, 3).map { x =>
    x + 1
  }

  // syntax sugar #2: single abstract method pattern
  trait Action {
    def act(x: Int): Int
  }

  val anInstance: Action = new Action {
    override def act(x: Int): Int = x + 1
  }

  val aFunc: Action = (x: Int) => x + 1 // scala compiler figures out that this lambda is def act

  // example: Runnables
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("hello, Scala")
  })

  val aSweeterThread = new Thread(() => println("sweet, Scala"))

  abstract class AnAbstractType {
    def implemented: Int = 23
    def f(a: Int): Unit
  }

  val abAbstractInstance: AnAbstractType = (a: Int) => println("sweet")

  // syntax sugar #3: The :: and #:: methods are special
  val prependedList = 2 :: List(3, 4)
  // 2.::(List(3, )) -> no :: method on Int
  // List(3, 4).::(2) -> compiler rewrites it this way

  // scala spec : last char decides associativity of method
  1 :: 2 :: 3 :: List(4, 5) // evaluates List(4, 5) first
  List(4, 5).::(3).::(2).::(1) // equivalent

  // members ending with : are right associative otherwise left
  class MyStream[T] {
    def -->:(value: T): MyStream[T] = this // actual implemetaiton here
  }

  val myStream = new MyStream[Int].-->:(1)
  val myStream1 = 1 -->: 2-->: 3-->: new MyStream[Int]

  // syntax sugar #4
  class Teen(name:String) {
    def `and then said`(gossip:String): Unit = println(s"$name said $gossip")
  }

  val lilly = new Teen("Lilly")
  lilly `and then said` "Scala is so sweet"

  // syntax sugar #5 infix types
  class Composite[A, B]
  val composite1: Composite[Int, String] = ???
  val composite2: Int Composite String = ???

  class -->[A, B]
  val towards: Int --> String = ???

  // syntax sugar #6: update() is very special, much like apply()
  val anArray = Array(1, 2, 3)
  anArray(2) = 7 // rewritten to anArray.update(2, 7)
  // update is used in mutable collections
  // remember apply() and update()!

  // syntax sugar #7: setters for mutable containers
  class Mutable {
    private var internalMember: Int = 0 // private for OO encapsulation

    def member = internalMember // getter
    def member_=(value: Int): Unit = internalMember = value // setter
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // rewritten as aMutableContainer.member_= 42
}
