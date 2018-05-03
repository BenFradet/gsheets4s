package gsheets4s
package interpreters

import cats.effect.IO
import cats.syntax.show._
import cats.syntax.option._
import io.circe.generic.auto._
import hammock._
import hammock.marshalling._
import hammock.jvm.Interpreter
import hammock.circe.implicits._

import algebras._
import model._

class RestSpreadsheetsValues private(
    accessToken: String)(implicit interpreter: Interpreter[IO]) extends SpreadsheetsValues[IO] {

  private val uri = (id: String, range: A1Notation) =>
    (Uri("https".some, none, "sheets.googleapis.com/v4/spreadsheets") /
      id / "values" / range.show).param("access_token", accessToken)

  def get(spreadsheetID: String, range: A1Notation): IO[Either[Error, ValueRange]] =
    get[Either[Error, ValueRange]](uri(spreadsheetID, range))

  def update(
    spreadsheetID: String,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): IO[Either[Error, UpdateValuesResponse]] = {
    val u = uri(spreadsheetID, range).param("valueInputOption", valueInputOption.value)
    put[ValueRange, Either[Error, UpdateValuesResponse]](u, updates)
  }

  private def get[O](uri: Uri)(implicit d: Decoder[O]): IO[O] = Hammock
    .request(Method.GET, uri, Map.empty)
    .as[O]
    .exec[IO]

  private def put[I, O](
    uri: Uri,
    body: I
  )(implicit c: Codec[I], d: Decoder[O]): IO[O] = Hammock
    .request(Method.PUT, uri, Map.empty, Some(body))
    .as[O]
    .exec[IO]
}

object RestSpreadsheetsValues {
  def apply(accessToken: String)(implicit interpreter: Interpreter[IO]): RestSpreadsheetsValues =
    new RestSpreadsheetsValues(accessToken)
}
