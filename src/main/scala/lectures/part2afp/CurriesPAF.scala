package lectures.part2afp

object CurriesPAF extends App {

  // curried functions
  val superAdder: Int => Int => Int =
    x => y => x + y

  val add3 = superAdder(3) // Int => Int = y => y + 3

  println(add3(5))
  println(superAdder(3)(5)) // curried function

  // METHOD!
  def curriedAdder(x: Int)(y: Int): Int = x + y // curried method

  // converted a method into a function Int => Int
  val add4: Int => Int = curriedAdder(4)

  // lifting, also known as ETA-EXPANSION

  // functions != methods due to JVM limitations
  def inc(x: Int) = x + 1
  List(1, 2, 3).map(inc) // compiler does ETA-expansion for us
  List(1, 2, 3).map(x => inc(x)) // compiler rewrites this for us to this function

  // Partial function applications
  val add5 = curriedAdder(5) _ // Int => Int

  // EXERCISE
  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def curriedAddMethod(x: Int)(y: Int) = x + y

  // add7: Int => Int = y => 7 + y
  // as many different implementations of add7 using the above
  // be creative!
  val add7 = (x: Int) => simpleAddFunction(7, x) // simplest
  val add7_2 = simpleAddFunction.curried(7)

  val add7_3 = curriedAddMethod(7) _ // partially applied function
  val add7_4 = curriedAddMethod(7)(_) // PAF = alternative syntax

  val add7_5 = simpleAddMethod(7, _: Int) // alternative syntax for turning methods into function values
        // y => simpleAddMethod(7, y)

  val add7_6 = simpleAddFunction(7, _: Int)

  // underscores are powerful
  def concatenator(a: String, b: String, c: String) = a + b + c
  val insertName = concatenator("Hello I'm ", _: String, ", how are you?") // x: String => concatenator(hello, x, how are you?)
  println(insertName("Ste"))

  val fillInTheBlanks = concatenator("Hello, ", _, _) //  (x, y) => concatenator("Hello, ", x, y)
  println(fillInTheBlanks("Ste", " Scala is awesome"))

  /**
   * Exercises
   *
   * 1. Process a list of numbers and return their string representations with different formats
   * Use the %4.2f, %8.6f and %14.12f with a curried formatter function.
   */

  def curriedFormatter(v: BigDecimal)(format: String) = v.formatted(format)
  val numbers = List[BigDecimal](Math.PI, Math.E, 1, 9.8, 1.3e-12)

  val simpleFormat = curriedFormatter(_: BigDecimal)("%4.2f") // lift
  val seriousFormat = curriedFormatter(_: BigDecimal)("%8.6f")
  val preciseFormat = curriedFormatter(_: BigDecimal)("%14.12f")


  println(numbers.map(simpleFormat))
  println(numbers.map(seriousFormat))
  println(numbers.map(preciseFormat))

  /**
   * 2. differences between
   *  - functions vs methods
   *  - parameters: by-name vs 0-lambda
   */

  def byName(n: => Int) = n + 1
  def byFunction(f: () => Int) = f() + 2

  def method: Int = 42
  def parenMethod(): Int = 43

  /**
   * Calling byName and byFunction
   *  - int
   *  - method
   *  - parenMethod
   *  - lambda
   *  - PAF
   */

  byName(23) // ok
  byName(method) // ok - method is evaluated to its call
  byName(parenMethod()) // ok
  byName(parenMethod) // also ok but beware, the method is called == byName(parenMethod())
  // byName(() => 42) // not ok, not the same as a function parameter
  byName((()=>42 )()) // ok, calling this lambda
  // byName(parenMethod _) // not ok

  // byFunction(45) // not ok
  // byFunction(method) // not ok, method is evaluated to 42, parameterless means its evaluated, not a function, compiler is not doing eta expansion
  byFunction(parenMethod) // compiler does eta expansion
  byFunction( () => 42 ) // ok
  byFunction(parenMethod  _) // also works but warning - unnecessary
}
