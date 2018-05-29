package gsheets4s
package interpreters

import io.circe.generic.auto._

import algebras._
import http._
import model._

class RestSpreadsheetsValues2[F[_]](token: String, client: HttpClient[F]) {
  def get(spreadsheetID: String, range: A1Notation): F[ValueRange] =
    client.get(token, s"$spreadsheetID/$range")

  def update(
    spreadsheetID: String,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): F[UpdateValuesResponse] =
    client.put(token, s"$spreadsheetID/$range", updates,
      List(("valueInputOption", valueInputOption.value)))
}
