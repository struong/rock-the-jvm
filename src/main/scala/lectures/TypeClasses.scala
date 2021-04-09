package lectures

// https://blog.rockthejvm.com/why-are-typeclasses-useful/
object TypeClasses extends App {

  // problem
  // specialised implementations

  // implicits
  trait Summable[T] {
    def sumElements(list: List[T]): T
  }

  // this pattern is called Type Class pattern which allows for ad-hoc polymorphism
  implicit object IntSummable extends Summable[Int] {
    override def sumElements(list: List[Int]): Int = list.sum
  }

  implicit object StringSummable extends Summable[String] {
    override def sumElements(list: List[String]): String = list.mkString("")
  }

  def processMyList[T](list: List[T])(implicit summable: Summable[T]): T = { // Summable[T] is ad-hoc polymorphism
    // "sum up" all the elements of the list
    // for integers => sum = actual sum of the elements
    // for strings => sum = concatenation of all the elements
    // for other types => error
    summable.sumElements(list)
  }

  override def main(args: Array[String]): Unit = {
    val intSum = processMyList(List(1, 2, 3))
    val stringSum = processMyList(List("Scala", " is", " awesome"))

    println(s"intSum = ${intSum}")
    println(s"stringSum = ${stringSum}")

    // processMyList(List(true, true, false)) // Errors at compile time
  }
}
