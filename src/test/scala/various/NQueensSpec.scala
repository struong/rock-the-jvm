package various

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import various.NQueens.Position

class NQueensSpec extends AnyFunSuite with Matchers {

  test("isValid empty board") {
    val board = List.fill(4, 4)(0)
    NQueens.isSafe(Position(0, 0), board) shouldBe true
  }

  test("isValid vertical has a queen") {
    val board = List.fill(4, 4)(0)
    NQueens.isSafe(Position(0, 0), board.updated(0, List(0, 0, 0, 1))) shouldBe false
  }

  test("isValid horizontal has a queen") {
    val board = List.fill(4, 4)(0)
    NQueens.isSafe(Position(0, 0), board.updated(1, List(1, 0, 0, 0))) shouldBe false
  }

  test("isValid left diagonal has a queen") {
    val board = List.fill(4, 4)(0)
    NQueens.isSafe(Position(0, 0), board.updated(3, List(0, 0, 0, 1))) shouldBe false
  }

  test("isValid right diagonal has a queen") {
    val board = List.fill(4, 4)(0)
    NQueens.isSafe(Position(3, 3), board.updated(0, List(1, 0, 0, 0))) shouldBe false
  }

  test("isValid right diagonal has a queen1") {
    val board = List.fill(8, 8)(0).updated(0, List(1, 0, 0, 0, 0, 0, 0, 0)).updated(1, List(0, 0, 1, 0, 0, 0, 0, 0))
    NQueens.isSafe(Position(2, 1), board) shouldBe false
  }

  test("0 Queen") {
    NQueens.nQueensStr(0) shouldBe
      """
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        |""".stripMargin
  }

  test("1 Queen") {
    NQueens.nQueensStr(1) shouldBe
      """
        | 1 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        |""".stripMargin
  }

  test("2 Queens") {
    NQueens.nQueensStr(2) shouldBe
      """
        | 1 0 0 0 0 0 0 0
        | 0 0 1 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        |""".stripMargin
  }

  test("3 Queens") {
    NQueens.nQueensStr(3) shouldBe
      """
        | 1 0 0 0 0 0 0 0
        | 0 0 1 0 0 0 0 0
        | 0 0 0 0 1 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        |""".stripMargin
  }

  test("4 Queens") {
    NQueens.nQueensStr(4) shouldBe
      """
        | 1 0 0 0 0 0 0 0
        | 0 0 1 0 0 0 0 0
        | 0 0 0 0 1 0 0 0
        | 0 1 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        |""".stripMargin
  }

  test("n Queens") {
    NQueens.nQueensStr(100) shouldBe
      """
        | 1 0 0 0 0 0 0 0
        | 0 0 1 0 0 0 0 0
        | 0 0 0 0 1 0 0 0
        | 0 1 0 0 0 0 0 0
        | 0 0 0 1 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        | 0 0 0 0 0 0 0 0
        |""".stripMargin
  }
}
