package gsheets4s

import eu.timepit.refined._
import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import eu.timepit.refined.string._

object model {
  type Row = Forall[UpperCase]
  type Col = Positive

  final case class Range(
    startRow: Row,
    startCol: Col,
    endRow: Row,
    endCol: Col
  )

  final case class A1Notation(
    sheetName: Option[String],
    range: Option[Range]
  )

  sealed abstract class Dimension(val value: String)
  case object Rows extends Dimension("ROWS")
  case object Columns extends Dimension("COLUMNS")

  final case class ValueRange(
    range: A1Notation,
    majorDimension: Dimension,
    values: List[List[String]]
  )

  final case class UpdateValuesResponse(
    spreadsheetId: String,
    updatedRange: A1Notation,
    updatedRows: Int,
    updatedColumns: Int,
    updatedCells: Int
  )
}
