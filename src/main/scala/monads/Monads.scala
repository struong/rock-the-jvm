package monads

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

object Monads {

  // covariant (like a List) safe accessible value in a multithreaded environment
  case class SafeValue[+T](private val internalValue: T) { // "constructor" = pure, or unit e.g. turn a value into something more interesting
    def get: T = synchronized { // protects this from multithreaded access
      // does something interesting
      internalValue
    }

    def flatMap[S](transformer: T => SafeValue[S]): SafeValue[S] = synchronized { // bind, or flatMap
      transformer(internalValue)
    }
  }

  // "external" API
  def gimmeSafeValue[T](value: T): SafeValue[T] = SafeValue(value)

  val safeString: SafeValue[String] = gimmeSafeValue("Scala is awesome")

  // if we want to do some operation like transform to upper string we must do the ETW pattern:

  // extract
  val string: String = safeString.get
  // transform
  val upperString: String = string.toUpperCase
  // wrap
  val upperSafeString: SafeValue[String] = SafeValue(upperString)

  // ETW pattern in one line
  // compressed:
  val upperSafeString2: SafeValue[String] = safeString.flatMap(s => SafeValue(s.toUpperCase))

  // Examples

  // Example 1: Census
  case class Person(firstName: String, lastName: String) {
    assert(firstName != null && lastName != null)
  }

  // Census API
  def getPerson(firstName: String, lastName: String): Person =
    if(firstName != null) {
      if(lastName != null) {
        Person(firstName, lastName)
      } else {
        null
      }
    } else {
      null
    }

  def getPersonBetter(firstName: String, lastName: String): Option[Person] =
    Option(firstName).flatMap(fName =>
      Option(lastName).flatMap( lName =>
        Option(Person(fName, lName))))

  def getPersonFor(firstName: String, lastName: String): Option[Person] =
    for {
      fName <- Option(firstName)
      lName <- Option(lastName)
    } yield Person(fName, lName)

  // Example 2: Asynchronous fetches

  case class User(id: String)
  case class Product(sku: String, price: Double)

  // External API
  def getUser(url: String): Future[User] = Future {
    User("Steven") // sample implementation
  }

  def getLastOrder(userId: String): Future[Product] = Future {
    Product("123-456", 99.99) // again a sample
  }

  val stevensUrl = "my.store.com/users/steven"

  // ETW pattern for Futures
  getUser(stevensUrl).onComplete {
    case Success(User(id)) =>
    val lastOrder = getLastOrder(id)
      lastOrder.onComplete {
      case Success(Product(sku, price)) =>
        val vatIncludedPrice = price * 1.19
        // then we do something with this vat value, like make an invoice
    }
  }

  val vatIncludedPrice: Future[Double] = getUser(stevensUrl)
    .flatMap ( user => getLastOrder(user.id))
    .map(_.price * 1.19)

  val vatIncludedPriceFor: Future[Double] = for {
    user <- getUser(stevensUrl)
    product <- getLastOrder(user.id)
  } yield product.price * 1.19

  // Example 3: double-for loops

  val numbers = List(1, 2, 3)
  val chars = List('a', 'b', 'c')

  // ETW pattern - extract a value from each list, build a tuple from them (transform), wrap all of them into a list

  val checkerBoard: List[(Int, Char)] = numbers.flatMap(number => chars.map(char => (number, char)))

  val checkerBoard2: List[(Int, Char)] = for {
    number <- numbers
    char <- chars
  } yield (number, char)

  // Monad properties

  // property 1: Left Identity
  def twoConsecutive(x: Int) = List(x, x + 1)
  twoConsecutive(3) // List(3, 4)
  List(3).flatMap(twoConsecutive) // List(3, 4)
  // Monad(x).flatMap(f) == f(x)

  // property 2: Right identity
  List(1, 2, 3).flatMap(x => List(x)) // List(1, 2, 3)
  // Monad(v).flatMap(x => Monad(x)) USELESS, returns Monad(v)

 // property 3: Associativity, e.g. ETW-ETW or implementing ETW over and over again
  val incrementer = (x: Int) => List(x, x + 1)
  val doubler = (x: Int) => List(x, 2 * x)

  def main(args: Array[String]): Unit = {
    println(
      List(1, 2, 3).flatMap(incrementer).flatMap(doubler) ==
      List(1, 2, 3).flatMap(x => incrementer(x).flatMap(doubler))
    )
    // List(1, 2, 2, 4,   2, 4, 3, 6,   3, 6, 4, 8)
    /**
     * List (
     *  incrementer(1).flatMap(doubler) -- 1,2,2,4
     *  incrementer(2).flatMap(doubler) -- 2,3,4,6
     *  incrementer(3).flatMap(doubler) -- 3,6,4,8
     * )
     *
     * Important because Monads are sequential operations (similar to the double-for loop example)
     * Monad(v).flatMap(f).flatMap(g) == Monad(v).flatMap(x => f(x).flatMap(g))
     */

  }

}
