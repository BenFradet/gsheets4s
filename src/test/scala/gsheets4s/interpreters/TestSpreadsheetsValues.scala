package gsheets4s
package interpreters

import cats.Id
import cats.syntax.foldable._

import algebras._
import model._

object TestSpreadsheetsValues extends SpreadsheetsValues[Id] {
  private var data: Map[String, List[List[String]]] = Map.empty

  def get(spreadsheetID: String, range: A1Notation): Id[Either[GsheetsError, ValueRange]] = {
    val values = data.get(spreadsheetID).getOrElse(List.empty)
    Right(ValueRange(range, Rows, values))
  }

  def update(
    spreadsheetID: String,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): Id[Either[GsheetsError, UpdateValuesResponse]] = {
    data = data + (spreadsheetID -> updates.values)
    val numRows = updates.values.size
    val numCols = updates.values.headOption.foldMap(_.size)
    Right(UpdateValuesResponse(spreadsheetID, range, numRows, numCols, numRows * numCols))
  }
}
