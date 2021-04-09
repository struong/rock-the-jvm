package lectures

object SelfTypes extends App {

  trait Edible

  // hierarchy #1
  trait Person {
    def hasAllergiesTo(thing: Edible): Boolean
  }

  trait Child extends Person

  trait Adult extends Person

  // hierarchy #2
  trait Diet {
    def eat(thing: Edible): Boolean
  }

  trait Carnivore extends Diet

  trait Vegetarian extends Diet

  // Problem: Diet must be applicable to Persons only

  // Want VegetarianAthlete to be enforced at compile time, if you
  // extend Vegetarian without extending Adult then the code should not compile
  // class VegetarianAthlete extends Vegetarian with Adult

  // Option #1 - enforce a subtype relationship

  trait DietOption1 extends Person { // why should a Diet extend a Person?
    def eat(thing: Edible): Boolean =
      if (this.hasAllergiesTo(thing)) false // have access to the logic in the Person class
      else 42 > 10
  }

  // Option #2 - add a type argument
  // <: -> subtype
  trait DietOption2[T <: Person] { // trait Diet must be applicable to a type T which extends Person
    def eat(thing: Edible): Boolean
  }

  trait CarnivoreOption2[T <: Person] extends DietOption2[T]

  trait VegetarianOption2[T <: Person] extends DietOption2[T]

  // would need a constructor argument to get access to Person
  // If we made type T covariant or contravariant -> trait DietOption2[+T <: Person]
  // we would run into other problems such as is a Vegetarian Diet applicable to an Adult/Child and which implementation is relevant or shared

  // or the other way round
  trait PersonOption2[D <: Diet] {
    def hasAllergiesTo(thing: Edible): Boolean
  }

  trait ChildOption2[D <: Diet] extends PersonOption2[D]

  trait AdultOption2[D <: Diet] extends PersonOption2[D]


  // Option #3 - self type, are markers to the compilers, whoever implements Diet, will also implement Person,
  // so they will have access to type Person in Diet via a self reference

  trait DietOption3 {
    this: Person => // self-type, looks like a lambda structure but has a different meaning, whoever extends Diet MUST ALSO extend (mixin) Person
    def eat(thing: Edible): Boolean =
      if (this.hasAllergiesTo(thing)) false // now have access to the self
      else 45 > 32
  }

  trait CarnivoreOption3 extends DietOption3 with Person

  trait VegetarianOption3 extends DietOption3 with Person

  class VegetarianAthleteOption3 extends VegetarianOption3 with Adult {
    override def hasAllergiesTo(thing: Edible): Boolean = false
  }

  // Why not just use inheritance
  trait Animal

  trait Dog extends Animal // Dog IS AN Animal

  // fundamental difference between inheritance and self types
  trait Diet2 {
    self: Person => // a diet REQUIRES a Person
  }

}
