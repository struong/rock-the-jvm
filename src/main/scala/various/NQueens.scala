package various

import scala.annotation.tailrec

object NQueens {
  final case class Position(row: Int, column: Int) {
    def next: Position = if(column == 7) Position(row + 1, 0) else Position(row, column + 1)
    def isEnd: Boolean = row == 7 && column == 7
  }

  def nQueens(n: Int): List[List[Int]] = {
       findQueens(Position(0, 0), n,  List.fill(8, 8)(0))
  }

  def isSafe(position: Position, currentBoard: List[List[Int]]): Boolean = {
    // queen already here
    if(currentBoard(position.row)(position.column) == 1) {
      false
    } else {
      val horizontal = for(peek <- 0 until currentBoard.length) yield {
        currentBoard(peek)(position.column) == 0
      }
      val vertical = for(peek <- 0 until currentBoard.length) yield {
        currentBoard(position.row)(peek) == 0
      }

      def liftBoard(row: Int, column: Int): Boolean =
        currentBoard.lift(row).flatMap(x => x.lift(column)).getOrElse(0) == 0

      val diagonal =  for(peek <- 0 until currentBoard.length) yield {
        liftBoard(position.row - peek, position.column - peek) &&
        liftBoard(position.row + peek, position.column + peek) &&
        liftBoard(position.row - peek, position.column + peek) &&
        liftBoard(position.row + peek, position.column - peek)
      }

      vertical.forall(_ == true) && horizontal.forall(_ == true) && diagonal.forall(_ == true)
    }
  }

  @tailrec
  def findQueens(position: Position, queens: Int, currentBoard: List[List[Int]]): List[List[Int]] =
    queens match {
      case 0 => currentBoard
      case _ if position.isEnd => currentBoard
      case _ =>
        if(isSafe(position, currentBoard)) {
          val newBoard = currentBoard.updated(position.row, currentBoard(position.row).updated(position.column, 1))
          findQueens(position.next, queens - 1, newBoard)
        } else {
          findQueens(position.next, queens, currentBoard)
        }
    }


  def nQueensStr(n: Int): String = {
    val formattedColumns =
      for (elem <- nQueens(n))
        yield elem.mkString(" ", " ", "")

    formattedColumns.mkString("\n", "\n", "\n")
  }
}
