package gsheets4s

import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import io.circe.{Decoder, Encoder}

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

  implicit val dimensionDecoder: Decoder[Dimension] = Decoder.decodeString.map {
    case Rows.value => Rows
    case Columns.value => Columns
  }

  implicit val dimensionEncoder: Encoder[Dimension] = Encoder.encodeString.contramap(_.value)

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
