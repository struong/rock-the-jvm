package various

import scala.annotation.tailrec

object NQueensRockTheJVM extends App {
  def nQueens(n: Int): List[String] = {
    def conflict(position: Int, queens: List[Int]): Boolean = {
      def conflictOneQueen(position: Int, queen: Int, index: Int): Boolean =
        queen == position || (index + 1 == position - queen) || (index + 1 == queen - position)

      queens.zipWithIndex.exists { pair =>
        val (queen, index) = pair
        conflictOneQueen(position, queen, index)
      }
    }

    /*
      ._._._._.
      |_|_|_|_|
      |_|_|_|_|
      |_|_|_|_|
      |_|_|_|_|

      nQueens(4)
      nqt(0, [], []) =
      nqt(0, [0], []) =
      nqt(1, [0], []) =
      nqt(2, [0], []) =
      nqt(0, [2, 0], []) =
      nqt(1, [2, 0], []) =
      nqt(2, [2, 0], []) =
      nqt(3, [2, 0], []) =
      nqt(4, [2, 0], []) =
      nqt(3, [0], []) =
      nqt(0, [3, 0], []) =
      nqt(1, [3, 0], []) =
      nqt(0, [1, 3, 0], []) =
      nqt(1, [1, 3, 0], []) =
      nqt(2, [1, 3, 0], []) =
      nqt(3, [1, 3, 0], []) =
      nqt(4, [3, 0], []) =
      nqt(2, [3, 0], []) =
      nqt(3, [3, 0], []) =
      nqt(4, [3, 0], []) =
      nqt(4, [0], []) =
      nqt(1, [], []) =

      possible solutions
      ._._._._.
      |_|x|_|_|
      |_|_|_|x|
      |x|_|_|_|
      |_|_|x|_|

      (1, 3, 0, 2)

      ._._._._.
      |_|_|x|_|
      |x|_|_|_|
      |_|_|_|x|
      |_|x|_|_|

      (2, 0, 3, 1)
     */
    @tailrec
    def nQueensTailRec(currentPosition: Int, currentQueen: List[Int], solutions: List[List[Int]]): List[List[Int]] = {
      // I'm out of options
      if (currentPosition >= n && currentQueen.isEmpty) solutions
      else if (currentPosition >= n) {
        // I'm out of options on THIS row, move the previous queen bty 1
        nQueensTailRec(currentQueen.head + 1, currentQueen.tail, solutions)
      }
      else if (conflict(currentPosition, currentQueen)) {
        // if in conflict with other queens, try next position
        nQueensTailRec(currentPosition + 1, currentQueen, solutions)
      }
      else if (currentQueen.length == n - 1) {
        // I've just built a solution
        val newSolution = currentPosition :: currentQueen
        nQueensTailRec(currentPosition + 1, currentQueen, newSolution :: solutions)
      } else {
        // Try next queen on the next row, as this one is valid
        nQueensTailRec(0, currentPosition :: currentQueen, solutions)
      }
    }


    def prettyPrint(solutions: List[Int]): String = {
      val topEdge = (1 to n).map(_ => " ").mkString(".", ".", ".")
      val rows = solutions.map { queen =>
        val cellsBefore = (0 until queen).map(_ => "_")
        val beforeString = if(cellsBefore.isEmpty) "|" else cellsBefore.mkString("|", "|", "|")
        val cellsAfter = ((queen + 1) until n).map(_ => "_")
        val afterString = if(cellsAfter.isEmpty) "|" else cellsAfter.mkString("|", "|", "|")

        beforeString + "x" + afterString
      }

      s"$topEdge\n${rows.mkString("\n")}"
    }

    nQueensTailRec(0, List.empty, List.empty).map(prettyPrint)
  }

  nQueens(8).foreach(println)
}