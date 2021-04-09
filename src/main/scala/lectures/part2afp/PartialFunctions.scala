package lectures.part2afp

object PartialFunctions extends App {

  val aFunction  = (x: Int) => x + 1 // Function1[Int, Int] == Int => Int

  val aFussyFunction = (x: Int) =>
    if(x == 1) 42
    else if(x == 2) 56
    else if (x == 5) 999
    else throw new FunctionNotApplicableException

  class FunctionNotApplicableException extends RuntimeException

  val aNicerFussyFunction = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }

  // {1, 2, 5} => Int
  // Known as a partial function because its a partial of the subset of Int

  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  } // Stuff inside of braces is called a partial function value

  println(aPartialFunction(2))
  // println(aPartialFunction(57813)) // throws a scala.MatchError because Partial Functions are based on Pattern Matching

  // Partial Function utilities
  println(aPartialFunction.isDefinedAt(67)) // tests if a partial function is applicable for value

  // Lift: lifts to full functions returning options
  val lifted: Int => Option[Int] = aPartialFunction.lift
  println(lifted(2))
  println(s"lifted(98) = ${lifted(98)}")

  // Chain: Using orElse, orElse takes another partial function as an argument
  val pfChain = aPartialFunction.orElse[Int, Int] {
    case 45 => 67
  }

  println(pfChain(2))
  println(pfChain(45))

  // PF extends normal functions
  val aTotalFunction: Int => Int = {
    case 1 => 99
  } // can apply a partial function because partial functions are a subtype of normal fuctions

  // HOFs accept partial functions as well
  val aMappedList = List(1, 2, 3).map {
    case 1 => 42
    case 2 => 78
    case 3 => 1000
    // would throw scala.MatchError: case 5 => 1000
  }

  println(s"aMappedList = ${aMappedList}")

  /*
    Note: Partial functions can only have ONE parameter type
   */

  /**
   * Exercises
   * 1 - construct a PF instance yourself (anonymous class)
   * 2 - dumb chatbot as a PF
   */

  val aManualFussyFunction = new PartialFunction[Int, Int] {
    override def apply(x: Int): Int = x match {
      case 1 => 42
      case 2 => 56
      case 5 => 999
    }
    override def isDefinedAt(x: Int): Boolean =
      x == 1 || x == 2 || x == 5
  }

  val aChatBot: PartialFunction[String, String] = {
    case "hello" => "goodbye"
    case "goodbye" => "don't leave"
  }

  // scala.io.Source.stdin.getLines().foreach(line => println("chatbot says: " + aChatBot(line)))
  scala.io.Source.stdin.getLines().map(aChatBot).foreach(println)

}
