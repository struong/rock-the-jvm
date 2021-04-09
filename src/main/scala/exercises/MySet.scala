package exercises

// https://github.com/rockthejvm/advanced-scala/blob/master/src/exercises/MySet.scala
trait MySet[A] extends (A => Boolean)  {

  /*
    Exercise: Implement a functional set
   */

  def apply(elem: A): Boolean = contains(elem)

  def contains(elem: A): Boolean
  def +(elem: A): MySet[A] // adding an element
  def ++(other: MySet[A]): MySet[A] // union

  def map[B](f: A => B): MySet[B]
  def flatMap[B](f: A => MySet[B]): MySet[B]
  def filter(predicate: A => Boolean): MySet[A]
  def forEach(f: A => Unit): Unit

  /*
    Exercise 2:
      - removing an element
      - intersection with another set
      - difference with another set
   */

  def -(elem: A): MySet[A]
  def --(anotherSet: MySet[A]): MySet[A] // difference
  def &(anotherSet: MySet[A]): MySet[A] // intersection

  /*
    Exercise 3:
      implement a unary_! = Negation of a set
      set[1, 2, 3] =>
   */
  def unary_! : MySet[A]
}

class EmptySet[A] extends MySet[A] {
  override def contains(elem: A): Boolean = false

  override def +(elem: A): MySet[A] = new NonEmptySet[A](elem, this)

  override def ++(other: MySet[A]): MySet[A] = other

  override def map[B](f: A => B): MySet[B] = new EmptySet[B]

  override def flatMap[B](f: A => MySet[B]): MySet[B] = new EmptySet[B]

  override def filter(predicate: A => Boolean): MySet[A] = this

  override def forEach(f: A => Unit): Unit = ()


  // part 2
  override def -(elem: A): MySet[A] = this

  override def --(anotherSet: MySet[A]): MySet[A] = this

  override def &(anotherSet: MySet[A]): MySet[A] = this

  def unary_! : MySet[A] = new PropertyBasedSet[A](_ => true)
}

class NonEmptySet[A](head: A, tail: MySet[A]) extends MySet[A] {
  override def contains(elem: A): Boolean =
    elem == head || tail.contains(elem)

  override def +(elem: A): MySet[A] =
    if(this.contains(elem)) this
    else new NonEmptySet[A](elem, this)

  /*
    [1, 2, 3] ++ [4, 5]
    [2, 3] ++ [4, 5] + 1
    [3] ++ [4, 5] + 1 + 2
    [] ++ [4, 5] + 1 + 2 + 3
    [4, 5] + 1 + 2 + 3
   */
  override def ++(other: MySet[A]): MySet[A] =
    tail ++ other + head

  override def map[B](f: A => B): MySet[B] =
    (tail map f) + f(head)

  override def flatMap[B](f: A => MySet[B]): MySet[B] =
    (tail flatMap f) ++ f(head)

  override def filter(predicate: A => Boolean): MySet[A] = {
    val filterTail = tail filter predicate

    if(predicate(head)) filterTail + head
    else filterTail
  }

  override def forEach(f: A => Unit): Unit = {
    f(head)
    tail forEach f
  }

  // part 2
  override def -(elem: A): MySet[A] =
    if(head == elem) tail
    else tail -elem + head

  override def --(anotherSet: MySet[A]): MySet[A] = filter(!anotherSet)

  override def &(anotherSet: MySet[A]): MySet[A] = filter(anotherSet) // intersection = filtering!

  // new operator
  def unary_! : MySet[A] = new PropertyBasedSet[A](x => !this.contains(x))
}

// all elements of type A which satisfy a property
// mathematical definition: { x in A | property(x) }
class PropertyBasedSet[A](property: A => Boolean) extends MySet[A] {
  override def contains(elem: A): Boolean = property(elem)
  // { x in A | property(x) } + element = { x in A | property(x) || x == element }
  override def +(elem: A): MySet[A] = new PropertyBasedSet[A](x => property(x) || x == elem)
  // { x in A | property(x) } ++ other = { x in A | property(x) || other contains x }
  override def ++(other: MySet[A]): MySet[A] = new PropertyBasedSet[A](x => property(x) || other(x))

  // all integers => (_ % 3) => [0 1 2]
  override def map[B](f: A => B): MySet[B] = politelyFail

  override def flatMap[B](f: A => MySet[B]): MySet[B] = politelyFail

  override def forEach(f: A => Unit): Unit = politelyFail

  override def filter(predicate: A => Boolean): MySet[A] = new PropertyBasedSet[A](x => property(x) && predicate(x))

  override def -(elem: A): MySet[A] = filter(x => x != elem)

  override def --(anotherSet: MySet[A]): MySet[A] = filter(!anotherSet) // difference

  override def &(anotherSet: MySet[A]): MySet[A] = filter(anotherSet) // intersection

  override def unary_! : MySet[A] = new PropertyBasedSet[A](x => !property(x))

  def politelyFail = throw new IllegalArgumentException("Really deep rabbit hole!")
}

object MySet {

  /*
    val s = MySet(1, 2, 3) = buildSet(Seq(1, 2, 3), [])
    = buildSet(Seq(2, 3), [] + 1)
    = buildSet(Seq(3), [1] + 2)
    = buildSet(Seq(), [1, 2] + 3)
    = [1, 2, 3]
   */


  def apply[A](values: A*): MySet[A] = {
    def buildSet(valSeq: Seq[A], acc: MySet[A]): MySet[A] = {
      if(valSeq.isEmpty) acc
      else buildSet(valSeq.tail, acc + valSeq.head)
    }

    buildSet(values.toSeq, new EmptySet[A])
  }
}

object MySetPlayground extends App {
  val s = MySet(1, 2, 3, 4)
  s + 5 forEach println
  println("*" * 50)
  s ++ MySet(-1, -2) forEach println
  println("*" * 50)
  s + 3 forEach println
  println("*" * 50)
  s map (x => x * 10) forEach println
  println("*" * 50)
  s flatMap (x => MySet(x, 10*x)) forEach println
  println("*" * 50)
  s filter (_ % 2 == 0) forEach println

  val negative = !s// s.unary_! = all the naturals not equal to 1, 2, 3, 4
  println(negative(2))
  println(negative(5))

  val negativeEven = negative.filter(_ % 2 == 0)
  println(negativeEven(5))

  val negativeEven5 = negative + 5 // all the even numbers > 4 + 5
  println(negativeEven5(5))

}
