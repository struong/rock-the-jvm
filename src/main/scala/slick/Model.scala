package slick

import java.time.LocalDate

final case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMinutes: Int)

object SlickTables {
  import slick.jdbc.PostgresProfile.api._

  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies") /* <- schema name */, "Movie") {

    def id = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def releaseDate = column[LocalDate]("release_date")
    def lengthInMinutes = column[Int]("length_in_min")

    // * tells Slick how to map a Movie case class with a Movie record from the DB
    override def * = (id, name, releaseDate, lengthInMinutes) <> (Movie.tupled, Movie.unapply)
  }

  // "API entry point"
  lazy val movieTable = TableQuery[MovieTable]
}