package gsheets4s
package integration

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.concurrent.Ref
import eu.timepit.refined.auto._
import org.scalatest._

import model._

class SpreadsheetsValuesSpec extends FlatSpec {

  val creds = for {
    accessToken <- sys.env.get("GSHEETS4S_ACCESS_TOKEN")
    refreshToken <- sys.env.get("GSHEETS4S_REFRESH_TOKEN")
    clientId <- sys.env.get("GSHEETS4S_CLIENT_ID")
    clientSecret <- sys.env.get("GSHEETS4S_CLIENT_SECRET")
  } yield Credentials(accessToken, refreshToken, clientId, clientSecret)
  assume(creds.isDefined)

  val spreadsheetID = "1tk2S_A4LZfeZjoMskbfFXO42_b75A7UkSdhKaQZlDmA"
  val not = SheetNameRangesNotation("Sheet1",
    NonEmptyList.one(Range(ColRowPosition("A", 1), ColRowPosition("B", 2))))
  val vr = ValueRange(not, Rows, List(List("1", "2"), List("3", "4")))
  val vio = UserEntered

  "RestSpreadsheetsValues" should "update and get values" in {
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

  it should "be able to retrieve multiple ranges" in {
    val not = SheetNameRangesNotation("Sheet1",
      NonEmptyList.of(
        Range(ColPosition("D"), ColPosition("D")),
        Range(ColPosition("G"), ColPosition("G"))
      )
    )
    val res = (for {
      credsRef <- Ref.of[IO, Credentials](creds.get)
      spreadsheetsValues = GSheets4s(credsRef).spreadsheetsValues
      prog <- new TestPrograms(spreadsheetsValues).get(spreadsheetID, not)
    } yield prog).unsafeRunSync()
    assert(res.isRight)
    val Right(vr) = res
    assert(vr.values.flatten == List("D", "G"))
  }

  it should "report an error if the spreadsheet it doesn't exist" in {
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

  it should "work with a faulty access token" in {
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
