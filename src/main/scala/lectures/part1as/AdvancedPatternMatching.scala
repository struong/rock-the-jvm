package lectures.part1as

object AdvancedPatternMatching extends App {

  val numbers = List(1)
  val description = numbers match {
    case head :: Nil => println(s"The only element is $head") // :: infix pattern
    case _ =>
  }

  /*
   What you can match on:
   - constants
   - wildcards
   - tuples
   - some special magic like above
   */

  // Imagine for some reason can't make this a case class
  // So how do we make this class ready for pattern matching
  class Person(val name: String, val age: Int)

  object Person {
    def unapply(person: Person): Option[(String, Int)] = {
      if (person.age < 21) None
      else Some((person.name, person.age))
    }

    // can create overloaded unapply patterns
    def unapply(age: Int): Option[(String)] =
      Some(if (age < 21) "minor" else "major")
  }

  val bob = new Person("Bob", 25) // if age less than 21, throw a MatchError

  val greeting = bob match {
    case Person(name, age) => // look for a method on an object called unapply
      s"Hi my name is $name and my age is $age years old"
  }

  println(greeting)

  val legalStatus = bob.age match {
    case Person(status) => // status "passed in here" is whats returned, minor or major
      s"My legal status is $status"
  }

  println(legalStatus)

  /*
  Exercise.
   */

  // industy standard for pattern here to be lower case
  object even {
    def unapply(arg: Int): Boolean = arg % 2 == 0
  }

  object singleDigit {
    def unapply(arg: Int): Boolean = arg > -10 && arg < 10
  }

  val n: Int = 8
  val mathProperty = n match {
    case singleDigit() => "single digit" // compiler knows this is now a single boolean test
    case even() => "an even number"
    case _ => "no property"
  }

  println(mathProperty)

  // infix patterns
  case class Or[A, B](a: A, b: B)

  val either = Or(2, "two")
  val humanDescription = either match {
    case number Or string => s"$number is written as $string" // same as Or(number, string)
  }

  println(humanDescription)

  // decomposing sequences
  val varargs = numbers match {
    case List(1, _*) => "starting with 1"
  }

  abstract class MyList[+A] {
    def head: A = ???
    def tail: MyList[A] = ???
  }

  case object Empty extends MyList[Nothing]
  case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] =
      if(list == Empty) Some(Seq.empty)
      else unapplySeq(list.tail).map(list.head +: _)
  }

  val myList: MyList[Int] = Cons(1, Cons(2, Cons(3, Empty)))
  val decomposed = myList match {
    case MyList(1, 2, _*) => "starting with 1 and 2" // looks for unapplySeq
    case _ => "something else"
  }

  println(decomposed)

  // custom return types for unapply
  // matcher object must contain these two functions
  // isEmpty: Boolean, get: something

  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }

  object PersonWrapper {
    def unapply(arg: Person): Wrapper[String] = new Wrapper[String] {
      override def isEmpty: Boolean = false
      override def get: String = arg.name
    }
  }

  println(bob match {
    case PersonWrapper(name) => s"This person's name is $name"
    case _ => "An alien"
  })
}
