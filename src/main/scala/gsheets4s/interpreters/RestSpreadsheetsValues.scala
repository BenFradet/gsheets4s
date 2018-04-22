package gsheets4s
package interpreters

import cats.effect.IO
import cats.syntax.show._
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
    s"https://sheets.googleapis.com/v4/spreadsheets/$id/values/${range.show}"

  def get(spreadsheetID: String, range: A1Notation): IO[ValueRange] = Hammock
    .request(Method.GET, uri"${uri(spreadsheetID, range)}", Map.empty)
    .as[ValueRange]
    .exec[IO]

  def update(
    spreadsheetID: String,
    range: A1Notation,
    updates: ValueRange
  ): IO[UpdateValuesResponse] = Hammock
    .request(Method.PUT, uri"${uri(spreadsheetID, range)}", Map.empty, Some(updates))
    .as[UpdateValuesResponse]
    .exec[IO]
}

object RestSpreadsheetsValues {
  def apply(accessToken: String)(implicit interpreter: Interpreter[IO]): RestSpreadsheetsValues =
    new RestSpreadsheetsValues(accessToken)
}
