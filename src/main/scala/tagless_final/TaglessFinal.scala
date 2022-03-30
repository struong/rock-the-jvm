package tagless_final

// https://www.youtube.com/watch?v=m3Qh-MmWpbM
object TaglessFinal {

  // expression problem
  object ExpressionProblem {
    trait Expr

    case class B(boolean: Boolean) extends Expr

    case class Or(left: Expr, right: Expr) extends Expr

    case class And(left: Expr, right: Expr) extends Expr

    case class Not(expr: Expr) extends Expr

    val aGianBoolean: Expr = Or(And(B(true), B(false)), B(false))

    def eval(expr: Expr): Boolean = expr match {
      case And(left, right) => eval(left) && eval(right)
      case B(boolean) => boolean
      case Not(expr) => !eval(expr)
      case Or(left, right) => eval(left) || eval(right)
    }

    // include ints
    case class I(int: Int) extends Expr

    case class Sum(left: Expr, right: Expr) extends Expr

    // we end up writing casts everywhere
    // loss of type safety means we have a crash at runtime
    def eval_v2(expr: Expr): Any = expr match {
      case B(boolean) => boolean
      case Or(left, right) => eval(left).asInstanceOf[Boolean] || eval(right).asInstanceOf[Boolean]
      // casts everywhere - and also need to check types
    }
  }

  /**
   * This solution is known as Tagging
   * - Expressions are validated for correctness at construction
   * - No type safety
   * - Errors are shown at runtime
   */
  object Tagging {
    trait Expr {
      val tag: String
    }

    case class B(boolean: Boolean) extends Expr {
      override val tag: String = "bool"
    }

    case class Or(left: Expr, right: Expr) extends Expr {
      // could use an assert but this still happens at runtime
      assert(left.tag == "bool" || right.tag == "bool")
      override val tag: String = "bool"
    }

    case class And(left: Expr, right: Expr) extends Expr {
      override val tag: String = "bool"
    }

    case class Not(expr: Expr) extends Expr {
      override val tag: String = "bool"
    }

    case class I(int: Int) extends Expr {
      override val tag: String = "int"
    }

    case class Sum(left: Expr, right: Expr) extends Expr {
      override val tag: String = "int"
    }

    // still unsafe and still crashes at runtime with the IllegalArgumentException
    def eval(expr: Expr): Any = expr match {
      case B(b) => b
      case Or(left, right) => if (left.tag != "bool" || right.tag != "bool")
        throw new IllegalArgumentException("improper argument type")
      else eval(left).asInstanceOf[Boolean] || eval(right).asInstanceOf[Boolean]
      // and so on for the rest
    }
  }

  object TaglessInitial {
    trait Expr[A]

    case class B(boolean: Boolean) extends Expr[Boolean]

    case class Or(left: Expr[Boolean], right: Expr[Boolean]) extends Expr[Boolean]

    case class And(left: Expr[Boolean], right: Expr[Boolean]) extends Expr[Boolean]

    case class Not(expr: Expr[Boolean]) extends Expr[Boolean]

    case class I(int: Int) extends Expr[Int]

    case class Sum(left: Expr[Int], right: Expr[Int]) extends Expr[Int]

    def eval[A](expr: Expr[A]): A = expr match {
      case B(b) => b
      case I(i) => i
      case Or(left, right) => eval(left) || eval(right)
      case Sum(left, right) => eval(left) + eval(right)
      // etc
    }
  }

  // just a fancy term for coding to an interface
  object TaglessFinal {
    trait Expr[A] {
      val value: A // the final value we care about
    }

    def b(boolean: Boolean): Expr[Boolean] = new Expr[Boolean] {
      override val value: Boolean = boolean
    }

    def i(int: Int): Expr[Int] = new Expr[Int] {
      override val value: Int = int
    }

    def or(left: Expr[Boolean], right: Expr[Boolean]): Expr[Boolean] = new Expr[Boolean] {
      override val value: Boolean = left.value || right.value
    }

    def and(left: Expr[Boolean], right: Expr[Boolean]): Expr[Boolean] = new Expr[Boolean] {
      override val value: Boolean = left.value && right.value
    }

    def sum(left: Expr[Int], right: Expr[Int]): Expr[Int] = new Expr[Int] {
      override val value: Int = left.value + right.value
    }

    // all sorted at compile time
    def eval[A](expr: Expr[A]): A = expr.value
  }

  // We usually see tagless final with a higher kinded type
  // F[_] : Monad = "tagless final"
  // concept of tagless final and type classes are separate

  // a typeclass example of the above
  object TaglessFinal_V2 {
    // E for expression
    trait UserLogin[E[_]] { // algebra == typeclass
      def checkLogin(mfa: Boolean): E[Boolean]
      def lastErrorStatus(code: Int): E[Int]
      def mfa_v1(email: E[Boolean], sms: E[Boolean]): E[Boolean]
      def mfa_v2(phone: E[Boolean], mobileApp: E[Boolean]): E[Boolean]
      def totalSessionLogins(server1Logins: E[Int], server2Logins: E[Int]): E[Int]
    }

    case class UserLoginStatus[A](value: A)
    // implement an instance of algebra
    implicit val loginCapabilityImpl: UserLogin[UserLoginStatus] = new UserLogin[UserLoginStatus] {
      override def checkLogin(boolean: Boolean): UserLoginStatus[Boolean] = UserLoginStatus(boolean)

      override def lastErrorStatus(int: Int): UserLoginStatus[Int] = UserLoginStatus(int)

      override def mfa_v1(left: UserLoginStatus[Boolean], right: UserLoginStatus[Boolean]): UserLoginStatus[Boolean] =
        UserLoginStatus(left.value || right.value)

      override def mfa_v2(left: UserLoginStatus[Boolean], right: UserLoginStatus[Boolean]): UserLoginStatus[Boolean] =
        UserLoginStatus(left.value && right.value)

      override def totalSessionLogins(left: UserLoginStatus[Int], right: UserLoginStatus[Int]): UserLoginStatus[Int] =
        UserLoginStatus(left.value + right.value)
    }

    def userLoginFlow[E[_]](implicit alg: UserLogin[E]): E[Boolean] = {
      import alg._
      mfa_v1(checkLogin(true), mfa_v2(checkLogin(true), checkLogin(false)))
    }

    def checkLastStatus[E[_]](implicit alg: UserLogin[E]): E[Int] = {
      import alg._
      totalSessionLogins(lastErrorStatus(24), lastErrorStatus(-3))
    }
  }

  def demoTagless(): Unit = {
    import TaglessInitial._
    println(eval(Or(B(true), And(B(true), B(false)))))
    println(eval(Sum(I(24), I(-3))))
  }

  def demoFinalTagless(): Unit = {
    import TaglessFinal._
    println(eval(or(b(true), and(b(true), b(false)))))
    println(eval(sum(i(24), i(-3))))
  }

  def demoFinalTagless_V2(): Unit = {
    import TaglessFinal_V2._
    println(userLoginFlow[UserLoginStatus].value)
    println(checkLastStatus[UserLoginStatus].value)
  }

  def main(args: Array[String]): Unit = {
    demoFinalTagless_V2()
  }
}
