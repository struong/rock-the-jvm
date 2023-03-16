package slick

import slick.jdbc.GetResult

import java.time.LocalDate
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object Main {

  import PrivateExecutionContext._
  import slick.jdbc.PostgresProfile.api._

  val shawshankRedemption = Movie(1L, "The Shawshank Redemption", LocalDate.of(1994, 9, 23), 162)
  val theMatrix = Movie(2L, "The Matrix", LocalDate.of(1999, 3, 31), 134)
  val phantomMenace = Movie(10L, "Star Wars: A Phantom Menace", LocalDate.of(1999, 5, 16), 133)

  val tomHanks = Actor(1L, "Tom Hanks")
  val juliaRoberts = Actor(2L, "Julia Roberts")
  val liamNeeson = Actor(3L, "Liam Neeson")

  def demoInsertMovie(): Unit = {
    // movieTable has the query to insert shawshankRedemption
    val queryDescription = SlickTables.movieTable += theMatrix
    val futureId: Future[Int] = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(newMovieId) => println(s"Query was successful, new id is $newMovieId")
      case Failure(exception) => println(s"Query failed, reason, $exception")
    }

    Await.result(futureId, 5.second)
  }

  def demoInsertActors(): Unit = {
    val queryDescription = SlickTables.actorTable ++= Seq(tomHanks, juliaRoberts)

    val futureId = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(_) => println(s"Query was successful")
      case Failure(exception) => println(s"Query failed, reason, $exception")
    }
  }

  def demoReadAllMovies(): Unit = {
    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.result) // Same as "select * from ___"

    resultFuture.onComplete {
      case Success(movies) => println(s"Fetched: $movies")
      case Failure(exception) => println(s"Query failed, reason, $exception")
    }

    Await.result(resultFuture, 5.second)
  }

  // Filter
  def demoReadSomeMovies(): Unit = {
    // % is regex in SQL, zero or more chars before/after
    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Matrix%")).result)
    // Same as "select * from ___ where name like "Matrix""

    resultFuture.onComplete {
      case Success(movies) => println(s"Fetched: $movies")
      case Failure(exception) => println(s"Query failed, reason, $exception")
    }

    Await.result(resultFuture, 5.second)
  }

  def demoUpdate(): Unit = {
    val queryDescriptor = SlickTables.movieTable.filter(_.id === 1L).update(shawshankRedemption.copy(lengthInMinutes = 150))

    val futureId: Future[Int] = Connection.db.run(queryDescriptor)

    futureId.onComplete {
      case Success(newMovieId) => println(s"Query was successful, new id is $newMovieId")
      case Failure(exception) => println(s"Query failed, reason, $exception")
    }

    Await.result(futureId, 5.second)
  }

  def demoDelete(): Unit = {
    val resultFuture = Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Matrix%")).delete)
    Await.result(resultFuture, 5.second)
  }

  def readMoviesByPlainQuery(): Future[Vector[Movie]] = {
    // [id,name,localDate,lengthInMin]
    // << automatically parses id
    // << modifies the state of PositionedResult to go to the next record
    implicit val getResultMovie: GetResult[Movie] = GetResult(positionedResult =>
      Movie(
        positionedResult.<<,
        positionedResult.<<,
        LocalDate.parse(positionedResult.nextString()),
        positionedResult.<<))

    val query = sql"""select * from movies."Movie"""".as[Movie]
    Connection.db.run(query)
  }

  def multipleQueriesSingleTransaction(): Unit = {
    val insertMovie = SlickTables.movieTable += phantomMenace
    val insertActor = SlickTables.actorTable += liamNeeson

    val finalQuery = DBIO.seq(insertMovie, insertActor)

    // transactionally -> if one fails, everything is rolled back
    Connection.db.run(finalQuery.transactionally)
  }

  def findAllActorsByMovie(movieId: Long): Future[Seq[Actor]] = {
    val joinQuery = SlickTables.movieActorMappingTable
      .filter(_.movieId === movieId)
      .join(SlickTables.actorTable)
      .on(_.actorId === _.id) // select * from movieActorMappingTable join actorTable a on m.actorId == a.id
      .map(_._2)

    Connection.db.run(joinQuery.result)
  }

  def main(args: Array[String]): Unit = {
    findAllActorsByMovie(4L).onComplete {
      case Failure(exception) => println(s"Query failed $exception")
      case Success(value) => println(s"Actors from Star Wars $value")
    }

    Thread.sleep(5000)
    PrivateExecutionContext.executor.shutdown()
  }
}
