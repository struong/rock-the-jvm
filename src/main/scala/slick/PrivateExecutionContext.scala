package slick

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object PrivateExecutionContext {
  val executor: ExecutorService = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(executor)
}
