package slick

import java.time.LocalDate
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object Main {
  import PrivateExecutionContext._
  import slick.jdbc.PostgresProfile.api._

  val shawshankRedemption = Movie(1L, "The Shawshank Redemption", LocalDate.of(1994, 9, 23), 162)
  val theMatrix = Movie(2L, "The Matrix", LocalDate.of(1999, 3, 31), 134)


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

  def main(args: Array[String]): Unit = {
    demoDelete()
  }
}
