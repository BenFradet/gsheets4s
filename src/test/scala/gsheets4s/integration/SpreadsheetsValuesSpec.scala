package gsheets4s
package integration

import cats.effect.IO
import eu.timepit.refined.auto._
import org.scalatest._

import model._
import org.scalatest.flatspec.AnyFlatSpec
import cats.effect.Ref

object Integration extends Tag(
  if (sys.env.get("GSHEETS4S_ACCESS_TOKEN").isDefined) "" else classOf[Ignore].getName)

class SpreadsheetsValuesSpec extends AnyFlatSpec {

  val creds = for {
    accessToken <- sys.env.get("GSHEETS4S_ACCESS_TOKEN")
    refreshToken <- sys.env.get("GSHEETS4S_REFRESH_TOKEN")
    clientId <- sys.env.get("GSHEETS4S_CLIENT_ID")
    clientSecret <- sys.env.get("GSHEETS4S_CLIENT_SECRET")
  } yield Credentials(accessToken, refreshToken, clientId, clientSecret)

  val spreadsheetID = "1tk2S_A4LZfeZjoMskbfFXO42_b75A7UkSdhKaQZlDmA"
  val not = SheetNameRangeNotation("Sheet1",
    Range(ColRowPosition("A", 1), ColRowPosition("B", 2)))
  val vr = ValueRange(not, Rows, List(List("1", "2"), List("3", "4")))
  val vio = UserEntered

  "RestSpreadsheetsValues" should "update and get values" taggedAs Integration in {
    val res = (for {
      credsRef <- Ref.of[IO, Credentials](creds.get)
      spreadsheetsValues = GSheets4s(credsRef).spreadsheetsValues
      prog <- new TestPrograms(spreadsheetsValues)
        .updateAndGet(spreadsheetID, vr, vio)
    } yield prog).unsafeRunSync()
    assert(res.isRight)
    val Right((uvr, vr2)) = res
    assert(uvr.spreadsheetId == spreadsheetID)
    assert(uvr.updatedRange == vr.range)
    assert(vr.range == vr2.range)
    assert(vr.values == vr2.values)
  }

  it should "report an error if the spreadsheet it doesn't exist" taggedAs Integration in {
    val res = (for {
      credsRef <- Ref.of[IO, Credentials](creds.get)
      spreadsheetsValues = GSheets4s(credsRef).spreadsheetsValues
      prog <- new TestPrograms(spreadsheetsValues)
        .updateAndGet("not-existing-spreadsheetid", vr, vio)
    } yield prog).unsafeRunSync()
    assert(res.isLeft)
    val Left(err) = res
    assert(err.code == 404)
    assert(err.message == "Requested entity was not found.")
    assert(err.status == "NOT_FOUND")
  }

  it should "work with a faulty access token" taggedAs Integration in {
    val res = (for {
      credsRef <- Ref.of[IO, Credentials](creds.get.copy(accessToken = "faulty"))
      spreadsheetsValues = GSheets4s(credsRef).spreadsheetsValues
      prog <- new TestPrograms(spreadsheetsValues)
        .updateAndGet(spreadsheetID, vr, vio)
    } yield prog).unsafeRunSync()
    assert(res.isRight)
    val Right((uvr, vr2)) = res
    assert(uvr.spreadsheetId == spreadsheetID)
    assert(uvr.updatedRange == vr.range)
    assert(vr.range == vr2.range)
    assert(vr.values == vr2.values)
  }
}
