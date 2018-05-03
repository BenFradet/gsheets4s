package gsheets4s
package integration

import cats.effect.IO
import eu.timepit.refined.auto._
import hammock.jvm.Interpreter
import org.scalatest._

import interpreters._
import model._

class SpreadsheetsValuesSpec extends FlatSpec {

  val token = sys.env.get("GSHEETS4S_ACCESS_TOKEN")
  assume(token.isDefined)

  val spreadsheetID = "1tk2S_A4LZfeZjoMskbfFXO42_b75A7UkSdhKaQZlDmA"
  val not = SheetNameRangeNotation("Sheet1",
    Range(ColRowPosition("A", 1), ColRowPosition("B", 2)))
  val vr = ValueRange(not, Rows, List(List("1", "2"), List("3", "4")))
  val vio = UserEntered

  implicit val interpreter = Interpreter[IO]

  "RestSpreadsheetsValues" should "update and get values" in {
    val res = new TestPrograms(RestSpreadsheetsValues(token.get))
      .updateAndGet(spreadsheetID, vr, vio)
      .unsafeRunSync()
    assert(res.isRight)
    val Right((uvr, vr2)) = res
    assert(uvr.spreadsheetId == spreadsheetID)
    assert(uvr.updatedRange == vr.range)
    assert(vr.range == vr2.range)
    assert(vr.values == vr2.values)
  }

  it should "report an error if the spreadsheet it doesn't exist" in {
    val res = new TestPrograms(RestSpreadsheetsValues(token.get))
      .updateAndGet("not-existing-spreadsheetid", vr, vio)
      .unsafeRunSync()
    assert(res.isLeft)
    val Left(err) = res
    assert(err.code == 404)
    assert(err.message == "Requested entity was not found.")
    assert(err.status == "NOT_FOUND")
  }
}
