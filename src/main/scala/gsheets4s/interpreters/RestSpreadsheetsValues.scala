package gsheets4s
package interpreters

import io.circe.generic.auto._
import io.lemonlabs.uri.typesafe.dsl._

import algebras.SpreadsheetsValues
import http._
import model._

class RestSpreadsheetsValues[F[_]](client: HttpClient[F]) extends SpreadsheetsValues[F] {
  def get(spreadsheetID: String, range: A1Notation): F[Either[GsheetsError, ValueRange]] =
    client.get(spreadsheetID / "values" / range)

  def update(
    spreadsheetID: String,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): F[Either[GsheetsError, UpdateValuesResponse]] =
    client.put(spreadsheetID / "values" / range, updates,
      List(("valueInputOption", valueInputOption.value)))
}
