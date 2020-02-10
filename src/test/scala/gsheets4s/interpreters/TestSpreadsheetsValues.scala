package gsheets4s
package interpreters

import cats.Id
import cats.syntax.foldable._
import cats.instances.int._
import cats.instances.option._
import eu.timepit.refined.types.string.NonEmptyString

import algebras._
import model._

class TestSpreadsheetsValues extends SpreadsheetsValues[Id] {
  private var data: Map[NonEmptyString, List[List[String]]] = Map.empty

  def get(spreadsheetID: NonEmptyString, range: A1Notation): Id[Either[GsheetsError, ValueRange]] = {
    val values = data.getOrElse(spreadsheetID, List.empty)
    Right(ValueRange(range, Rows, values))
  }

  def update(
    spreadsheetID: NonEmptyString,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): Id[Either[GsheetsError, UpdateValuesResponse]] = {
    data = data + (spreadsheetID -> updates.values)
    val numRows = updates.values.size
    val numCols = updates.values.headOption.foldMap(_.size)
    Right(UpdateValuesResponse(spreadsheetID.value, range, numRows, numCols, numRows * numCols))
  }

  override def append(spreadsheetID: NonEmptyString,
                      range: A1Notation,
                      values: List[List[String]],
                      majorDimension: Dimension,
                      valueInputOption: ValueInputOption,
                      insertDataOption: InsertDataOption): Id[Either[GsheetsError, AppendValuesResponse]] = {
    data = data + (spreadsheetID -> values)
    val numRows = values.size
    val numCols = values.headOption.foldMap(_.size)
    Right(AppendValuesResponse(spreadsheetID.value, range, UpdateValuesResponse(spreadsheetID.value, range, numRows, numCols, numRows * numCols)))
  }
}
