package lectures

import io.estatico.newtype.macros.newtype

// https://blog.rockthejvm.com/value-classes/
object ValueClasses extends App {

  // online store
  final case class Product(code: String, description: String)

  trait Backend {
    def findByCode(code: String): Option[Product]

    def findByDescription(description: String): Option[Product]
  }

  val aCode = "1-12345-12345"
  val aDescription = "foam mattress for a nice sleep"

  val backendV1 = new Backend {
    override def findByCode(code: String): Option[Product] = None

    override def findByDescription(description: String): Option[Product] = None
  }

  backendV1.findByCode(aCode)
  backendV1.findByDescription(aCode) // compiles but this is a bug

  // problem.

  // solution 1 - use case classes

  final case class Barcode(code: String)

  object Barcode {
    def apply(code: String): Either[String, Barcode] = {
      Either.cond(
        code.matches("\\d-\\d{5}-\\d{5}"),
        new Barcode(code),
        "Code is invalid"
      )
    }
  }

  final case class Description(txt: String)

  trait BackendV2 {
    def findByCode(code: Barcode): Option[Product]

    def findByDescription(description: Description): Option[Product]
  }

  val backendV2 = new BackendV2 {
    override def findByCode(code: Barcode): Option[Product] = {
      println("Found a product in BackendV2")
      None
    }

    override def findByDescription(description: Description): Option[Product] = None
  }

  //  backendV2.findByCode(Barcode(aCode)) // better
  //  backendV2.findByCode(Barcode(aDescription)) // compiles but BAD

  Barcode(aCode).foreach(backendV2.findByCode(_))

  // 2 - value classes (VCs)
  final case class BarcodeVC(code: String) extends AnyVal {
    // no other vals, just methods
    def countryCode: Char = code.charAt(0)
  }

  // - no runtime overhead
  /*
  Restrictions on VCs:
   - only ONE val constructor argument
   - no other vals inside, just method (defs)
   - cannot be extended
   - can only extend "universal traits" (traits with just defs and without initialisations)
   */

  // drawback 1
  def show[T](arg: T): String = arg.toString

  show(BarcodeVC("1-12345-12345")) // BarcodeVC will be instantiated

  // drawback 2 with arrays
  val barcodes = Array[BarcodeVC](BarcodeVC("1-12345-12345")) // instantiates classes again

  // drawback 3, pattern matching
  BarcodeVC("1-12345-12345") match {
    case BarcodeVC(code) => println(code)
  }

  // 3 - NewTypes
  /* The Haskell language provides a newtype keyword for creating new types from existing ones without runtime overhead.
    newtype WidgetId = WidgetId Int

    lookupWidget :: WidgetId -> Maybe Widget
    lookupWidget (WidgetId wId) = lookup wId widgetDB

    In the example above, the WidgetId type is simply an Int at runtime; however, the compiler will treat it as its own type
    at compile time, helping you to avoid errors. In this case, we can be sure that the ID we are providing to our
    lookupWidget function refers to a WidgetId and not some other entity nor an arbitrary Int value.

    This library attempts to bring newtypes to Scala.
   */

  import io.estatico.newtype.ops._

  @newtype final case class BarcodeNT(code: String) // VC without issues 1-3, no runtime overhead

  object BarcodeNT {
    def mkBarCode(code: String): Either[String, BarcodeNT] = {
      Either.cond(
        code.matches("\\d-\\d{5}-\\d{5}"),
        code.coerce, // implicitly convert to a BarcodeNT using newtype.ops._
        "Code is invalid"
      )
    }
  }

}
