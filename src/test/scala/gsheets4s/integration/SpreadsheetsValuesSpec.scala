package gsheets4s
package integration

import cats.effect.IO
import eu.timepit.refined.auto._
import hammock.jvm.Interpreter
import org.scalatest._

import interpreters._
import model._

object SpreadsheetsValuesSpec extends FlatSpec {

  val token = sys.env.get("GSHEETS4S_ACCESS_TOKEN")
  assume(token.isDefined)

  val spreadsheetId = "1tk2S_A4LZfeZjoMskbfFXO42_b75A7UkSdhKaQZlDmA"

  implicit val interpreter = Interpreter[IO]

  "RestSpreadsheetsValues" should "update and get values" in {
    val not = SheetNameRangeNotation("Sheet1",
      Range(ColRowPosition("A", 1), ColRowPosition("B", 2)))
    val vr = ValueRange(not, Rows, List(List("1", "2"), List("3", "4")))
    val (uvr, vr2) =
      new TestPrograms(RestSpreadsheetsValues(token.get)).updateAndGet(spreadsheetId, vr)
        .unsafeRunSync()
    assert(uvr.spreadsheetId == spreadsheetId)
    assert(uvr.updatedRange == vr.range)
    assert(vr.range == vr2.range)
    assert(vr.values == vr2.values)
  }
}
