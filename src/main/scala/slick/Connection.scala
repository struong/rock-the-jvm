package slick

import slick.jdbc.PostgresProfile.api._

object Connection {
  // Looks for the postgres block from application.conf
  val db = Database.forConfig("postgres")
}
