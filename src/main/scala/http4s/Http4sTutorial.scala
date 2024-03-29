package http4s

import cats._
import cats.effect._
import cats.implicits._
import org.http4s.circe._
import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl._
import org.http4s.dsl.impl._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.blaze.BlazeServerBuilder

import java.time.Year
import java.util.UUID
import scala.collection.mutable
import scala.util.Try

// https://blog.rockthejvm.com/http4s-tutorial/
object Http4sTutorial extends IOApp {

  // movie database
  type Actor = String
  final case class Movie(id: String, title: String, year: Int, actors: List[Actor], director: String)
  final case class Director(firstName: String, lastName: String) {
    override def toString: Actor = s"$firstName $lastName"
  }

  final case class DirectorDetails(firstName: String, lastName: String, genre: String)

  // internal "database"
  val snjl: Movie = Movie(
    "6bcbca1e-efd3-411d-9f7c-14b872444fce",
    "Zack Snyder's Justice League",
    2022,
    List("Henry Cavill", "Gal Godot", "Ezra Miller", "Ben Affleck", "Ray Fisher", "Jason Momoa"),
    "Zack Snyder"
  )

  val moviesDB: Map[String, Movie] = Map(snjl.id -> snjl)

  // "business logic"
  private def findMovieById(movieId: UUID) =
    moviesDB.get(movieId.toString)

  private def findMoviesByDirector(director: String): List[Movie] =
    moviesDB.values.filter(_.director == director).toList


  /*
    Endpoints
    - GET all movies for a director under a given year
    - GET list of actors for a movie
    - GET details about a director
    - POST add a new director
   */

    // Every Request received, will return a response in a purely functional manner (so wrapped in a F): Request -> F[Option[Response]]
    // Not all request have a response, so it can be an option
    // HttpRoutes[F] = Request -> F[Option[Response]]


  // emap from http4s, produces a Either[ParseFailure, U]
  implicit val yearQueryParamDecoder: QueryParamDecoder[Year] = QueryParamDecoder[Int].emap { yearInt =>
      Try(Year.of(yearInt))
        .toEither
        .leftMap { e =>
          ParseFailure(e.getMessage, e.getMessage)
        }
  }

  object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")
  object YearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

  // GET /movies?director=Zack%20Snyder&year=2022
  // GET /movies/:moveiId/actors

  def movieRoutes[F[_] : Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F]{
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& YearQueryParamMatcher(maybeYear) =>
        val moviesByDirector = findMoviesByDirector(director)

        maybeYear match {
          case Some(validatedYear) =>
            validatedYear.fold(_ => BadRequest("The year was badly formatted"), { year =>
              val moviesByDirectorAndYear = moviesByDirector.filter(_.year == year.getValue)

              Ok(moviesByDirectorAndYear.asJson)
            })
          case None => Ok(moviesByDirector.asJson)
        }

      case GET -> Root / "movies" / UUIDVar(movieId) / "actors" =>
        findMovieById(movieId).map(_.actors) match {
          case Some(movies) => Ok(movies.asJson)
          case None => NotFound(s"No movies with id $movieId found in the databse")
        }
    }
  }

  object DirectorPath {
    def unapply(str: String): Option[Director] = {
      Try {
        val tokens = str.split(" ")
        Director(tokens.head, tokens(1))
      }.toOption
    }
  }

  val directorDetailsDB: mutable.Map[Director, DirectorDetails] =
    mutable.Map(Director("Zack", "Snyder") -> DirectorDetails("Zack", "Snyder", "superhero"))

  def directorRoutes[F[_] : Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "directors" / DirectorPath(director) =>
        directorDetailsDB.get(director) match {
          case Some(directorDetails) => Ok(directorDetails.asJson)
          case None => NotFound(s"No director '$director' found")
        }
    }
  }

  def allRoutes[F[_] : Monad]: HttpRoutes[F] = {
    // <+>/combineK is combine from cats.syntax.semigroupk._
    movieRoutes[F].combineK(directorRoutes[F])
  }

  // HttpApp is a simplified version of HttpRoutes
  def allRoutesComplete[F[_] : Monad]: HttpApp[F] = {
    allRoutes[F].orNotFound
  }

  override def run(args: List[String]): IO[ExitCode] = {
    // Wiring up the routes, could also use allRoutesComplete
    val apis = Router(
      "/api" -> movieRoutes[IO],
      "/api/admin" -> directorRoutes[IO]
    ).orNotFound

    // runtime.compute is the ec from cats-effect
    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(allRoutesComplete) // alternative: apis
      .resource // convert this into a cats-effect resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
