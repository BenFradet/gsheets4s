package gsheets4s
package algebras

import model._

trait SpreadsheetsValues[F[_]] {
  def get(spreadsheetID: String, range: A1Notation): F[ValueRange]
  def update(spreadsheetID: String, range: A1Notation, updates: ValueRange): F[UpdateValuesResponse]
}
