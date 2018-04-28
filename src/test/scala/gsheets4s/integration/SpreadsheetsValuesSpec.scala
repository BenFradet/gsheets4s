package gsheets4s
package integration

import cats.effect.IO
import cats.syntax.option._
import cats.syntax.show._
import eu.timepit.refined.auto._
import hammock._
import hammock.jvm.Interpreter
import org.scalatest._

import interpreters._
import model._

class SpreadsheetsValuesSpec extends FlatSpec {

  val token = sys.env.get("GSHEETS4S_ACCESS_TOKEN")
  assume(token.isDefined)

  val spreadsheetID = "1tk2S_A4LZfeZjoMskbfFXO42_b75A7UkSdhKaQZlDmA"

  implicit val interpreter = Interpreter[IO]

  private val uri = (id: String, range: A1Notation) =>
    (Uri("https".some, none, "sheets.googleapis.com/v4/spreadsheets") /
      id / "values" / range.show).param("access_token", token.get)

  "RestSpreadsheetsValues" should "update and get values" in {
    val not = SheetNameRangeNotation("Sheet1",
      Range(ColRowPosition("A", 1), ColRowPosition("B", 2)))
    val vr = ValueRange(not, Rows, List(List("1", "2"), List("3", "4")))
    val vio = UserEntered
    val (uvr, vr2) =
      new TestPrograms(RestSpreadsheetsValues(token.get)).updateAndGet(spreadsheetID, vr, vio)
        .unsafeRunSync()
    assert(uvr.spreadsheetId == spreadsheetID)
    assert(uvr.updatedRange == vr.range)
    assert(vr.range == vr2.range)
    assert(vr.values == vr2.values)
  }
}
