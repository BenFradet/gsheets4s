package gsheets4s
package algebras

import eu.timepit.refined.types.string.NonEmptyString

import model._

trait SpreadsheetsValues[F[_]] {
  def get(spreadsheetID: NonEmptyString, range: A1Notation): F[Either[GsheetsError, ValueRange]]
  def update(
    spreadsheetID: NonEmptyString,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): F[Either[GsheetsError, UpdateValuesResponse]]

  def append(spreadsheetID: NonEmptyString,
             range: A1Notation,
             values: List[List[String]],
             majorDimension: Dimension = Rows,
             valueInputOption: ValueInputOption = Raw,
             insertDataOption: InsertDataOption = InsertRows,
            ): F[Either[GsheetsError, AppendValuesResponse]]
}
