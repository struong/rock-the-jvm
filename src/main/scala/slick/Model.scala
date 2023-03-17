package slick

import java.time.LocalDate

final case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMinutes: Int)

final case class Actor(id: Long, name: String)

final case class MovieActorMapping(id: Long, movieId: Long, actorId: Long)

final case class StreamingProviderMapping(id: Long, movieId: Long, streamingProvider: StreamingService.Provider)

object StreamingService extends Enumeration {
  type Provider = Value
  val Netflix = Value("Netflix")
  val Disney = Value("Disney")
  val Prime = Value("Prime")
  val Hulu = Value("Hulu")
}

object SlickTables {

  import slick.jdbc.PostgresProfile.api._

  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies") /* <- schema name */ , "Movie") {

    def id: Rep[Long] = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column[String]("name")

    def releaseDate: Rep[LocalDate] = column[LocalDate]("release_date")

    def lengthInMinutes: Rep[Int] = column[Int]("length_in_min")

    // * tells Slick how to map a Movie case class with a Movie record from the DB
    override def * = (id, name, releaseDate, lengthInMinutes) <> (Movie.tupled, Movie.unapply)
  }

  // "API entry point"
  lazy val movieTable = TableQuery[MovieTable]

  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor") { // ___ into movies.Actors
    def id = column[Long]("actor_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    override def * = (id, name) <> (Actor.tupled, Actor.unapply)
  }

  lazy val actorTable = TableQuery[ActorTable]

  class MovieActorMappingTable(tag: Tag) extends Table[MovieActorMapping](tag, Some("movies"), "MovieActorMapping") { // tableName, just copy it from init sql
    def id = column[Long]("movie_actor_id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie_id")
    def actorId = column[Long]("actor_id")

    override def * = (id, movieId, actorId) <> (MovieActorMapping.tupled, MovieActorMapping.unapply)
  }

  lazy val movieActorMappingTable = TableQuery[MovieActorMappingTable]

  class StreamingProviderMappingTable(tag: Tag) extends Table[StreamingProviderMapping](tag, Some("movies"), "StreamingProviderMapping") {

    implicit val providerMapper = MappedColumnType.base[StreamingService.Provider, String](
      provider => provider.toString,
      string => StreamingService.withName(string)
    )

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc) // name should be identical to the sql script
    def movieId = column[Long]("movie_id")
    def streamingProvider = column[StreamingService.Provider]("streaming_provider")

    override def * = (id, movieId, streamingProvider) <> (StreamingProviderMapping.tupled, StreamingProviderMapping.unapply)
  }

  lazy val streamingProviderMappingTable = TableQuery[StreamingProviderMappingTable]

  // table generation scripts
  val tables = List(movieTable, actorTable, movieActorMappingTable, streamingProviderMappingTable)
  // data definition language
  val ddl = tables.map(_.schema).reduce(_ ++ _) // combine all the schemes to one giant schema
}

object TableDefinitionGenerator {
  def main(args: Array[String]): Unit = {
    println(SlickTables.ddl.createIfNotExistsStatements.mkString(";\n"))
  }
}
