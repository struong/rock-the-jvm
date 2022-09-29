package fs2

import cats.effect.{IO, IOApp}
import fs2.Stream

// https://blog.rockthejvm.com/fs2/
object FS2Demo extends IOApp.Simple {
  final case class Actor(id: Int, firstName: String, lastName: String)

  object Data {
    // Justice League
    val henryCavil: Actor = Actor(0, "Henry", "Cavill")
    val galGodot: Actor = Actor(1, "Gal", "Godot")
    val ezraMiller: Actor = Actor(2, "Ezra", "Miller")
    val benFisher: Actor = Actor(3, "Ben", "Fisher")
    val rayHardy: Actor = Actor(4, "Ray", "Hardy")
    val jasonMomoa: Actor = Actor(5, "Jason", "Momoa")

    // Avengers
    val scarlettJohansson: Actor = Actor(6, "Scarlett", "Johansson")
    val robertDowneyJr: Actor = Actor(7, "Robert", "Downey Jr.")
    val chrisEvans: Actor = Actor(8, "Chris", "Evans")
    val markRuffalo: Actor = Actor(9, "Mark", "Ruffalo")
    val chrisHemsworth: Actor = Actor(10, "Chris", "Hemsworth")
    val jeremyRenner: Actor = Actor(11, "Jeremy", "Renner")
    val tomHolland: Actor = Actor(13, "Tom", "Holland")
    val tobeyMaguire: Actor = Actor(14, "Tobey", "Maguire")
    val andrewGarfield: Actor = Actor(15, "Andrew", "Garfield")
  }

  // streams = abstraction to manage an unbounded amount of data
  // IO = any kind of computation that might perform side effects
  implicit class IODebugOps[A](io: IO[A]) {
    def debug: IO[A] = io.map { a =>
      println(s"[${Thread.currentThread().getName}] $a")
      a
    }
  }

  import Data._

  // streams
  // pure streams = store actual data (a finite amount)
  // Pure is an effect type unlike IO, Option etc, it does nothing
  val jlActors = Stream[Pure, Actor](
    henryCavil,
    galGodot,
    ezraMiller,
    benFisher,
    rayHardy,
    jasonMomoa
  )

  // emit = creates a Stream[Pure, Actor]
  val tomHollandStream: Stream[Pure, Actor] = Stream.emit(tomHolland)
  val spiderMen: Stream[Pure, Actor] = Stream.emits(List(tomHolland, andrewGarfield, tobeyMaguire))

  // convert a stream to a std data structure
  val jsActorList = jlActors.toList // only applicable for Stream[Pure, _]

  // infinite streams
  val infiniteJLActors: Stream[Pure, Actor] = jlActors.repeat
  val repeatedJLActorsList: List[Actor] = infiniteJLActors.take(10).toList

  // effectful streams
  val savingTomHolland: Stream[IO, Actor] = Stream.eval {
    IO {
      println("Saving actor Tom Holland into the DB")
      Thread.sleep(1000)
      tomHolland
    }
  }

  // Need to Compile the stream
  val compileStream: IO[Unit] = savingTomHolland.compile.drain

  // Streams can be made with chunks of data
  val avengerActors: Stream[Pure, Actor] = Stream.chunk(Chunk.array(Array(
    scarlettJohansson,
    robertDowneyJr,
    chrisEvans,
    markRuffalo,
    chrisHemsworth,
    jeremyRenner,
    tomHolland,
    tobeyMaguire,
    andrewGarfield
  )))

  // transformations
  val allSuperheroes = jlActors ++ avengerActors

  // flatMap
  val printedJLActors: Stream[IO, Unit] = jlActors.flatMap { actor =>
    // perform an IO[Unit] effect
    Stream.eval(IO.println(actor))
  }

  // flatMap + eval = evalMap
  val printedJlActors_v2: Stream[IO, Unit] = jlActors.evalMap(IO.println)

  // flatMap + eval while keeping the original type = evalTap
  val printedJLActors_v3: Stream[IO, Actor] = jlActors.evalTap(IO.println)

  override def run: IO[Unit] = compileStream
  // IO(repeatedJLActorsList).debug.void
}
