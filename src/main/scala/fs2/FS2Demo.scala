package fs2

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import fs2.Stream

import scala.concurrent.duration.DurationInt
import scala.util.Random


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

  // pipe is just function that transforms a stream
  // pipe = Stream[F, I] => Stream[F, O]
  val actorToStringPipe: Pipe[IO, Actor, String] = inStream => inStream.map(actor => s"${actor.firstName} ${actor.lastName}")

  def toConsole[A]: Pipe[IO, A, Unit] = inStream => inStream.evalMap(IO.println)

  // connect streams together with pipes and through, through takes a pipe
  // same as via from Akka streams
  val stringNamesPrinted: Stream[IO, Unit] = jlActors.through(actorToStringPipe).through(toConsole)


  // error handling
  def saveToDatabase(actor: Actor): IO[Int] = IO {
    println(s"Saving ${actor.firstName} ${actor.lastName}")
    // fails 50% of the time
    if (Random.nextBoolean()) {
      throw new RuntimeException("Persistence layer failed")
    } else {
      println("Saved.")
      actor.id
    }
  }

  val savedJLActors: Stream[IO, Int] = jlActors.evalMap(saveToDatabase)

  // handleErrorWith, takes a function from a throwable to something else
  // we can return a stream of different type, it will be the lowest selected ancestor
  val errorHandledActors: Stream[IO, Int] = savedJLActors.handleErrorWith(error => Stream.eval(IO {
    println(s"Error occured: $error")
    -1
  }))

  // attempt, turn a stream to Either
  val attemptedSavedJLActors: Stream[IO, Either[Throwable, Int]] = savedJLActors.attempt
  val attemptedProcessed = attemptedSavedJLActors.evalMap {
    case Left(error) => IO(s"Error: $error").debug()
    case Right(value) => IO(s"Successfully processed actor ID: $value").debug()
  }

  // resource management
  case class DatabaseConnection(url: String)

  def acquireConnection(url: String): IO[DatabaseConnection] = IO {
    println("Getting DB connection...")
    DatabaseConnection(url)
  }

  def release(connection: DatabaseConnection): IO[Unit] = IO {
    println(s"Release connection to ${connection.url}")
  }

  // bracket pattern
  val managedJLActors: Stream[IO, Int] =
    Stream.bracket(acquireConnection("jdbc://mydatabase.com"))(release).flatMap { conn =>
      // process a stream using this resource
      savedJLActors.evalTap(actorId => IO(s"Saving actor $actorId to ${conn.url}").debug())
    }

  // merge and concurrent stream execution

  // assume that these come from different parts of the application, we can merge them concurrently
  val concurrentJlActors = jlActors.evalMap { actor =>
    IO {
      Thread.sleep(400)
      actor
    }.debug()
  }

  val concurrentAvengersActors = avengerActors.evalMap { actor =>
    IO {
      Thread.sleep(200)
      actor
    }.debug()
  }

  val mergedActors: Stream[IO, Actor] = concurrentJlActors.merge(concurrentAvengersActors)

  // concurrently, runs two streams at the same time
  // useful if two streams with two different effects, have something in the backend that uses two different resources
  // example: producer-consumer
  val queue: IO[Queue[IO, Actor]] = Queue.bounded(10) // buffer of at most 10 elements big
  val concurrentSystem = Stream.eval(queue).flatMap { q =>
    // producer stream
    val producer: Stream[IO, Unit] = jlActors
      .evalTap(actor => IO(actor).debug())
      .evalMap(actor => q.offer(actor)) // enqueue
      .metered(1.second) // throttle at 1 effect per second

    // consumer stream
    val consumer: Stream[IO, Unit] = Stream.fromQueueUnterminated(q)
      .evalMap(actor => IO(s"Consumed actor $actor").debug().void)

    producer.concurrently(consumer)
  }

  override def run: IO[Unit] = concurrentSystem.compile.drain
  //    compileStream
  // IO(repeatedJLActorsList).debug.void
}
