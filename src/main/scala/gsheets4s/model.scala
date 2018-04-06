package gsheets4s

import cats.implicits._
import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import io.circe.{Decoder, Encoder}

object model {
  type Row = Forall[UpperCase]
  type Col = Positive

  sealed trait Position {
    def stringRepresentation: String
  }
  final case class RowPosition(row: Row) extends Position {
    override def stringRepresentation: String = row.toString
  }
  final case class ColPosition(col: Col) extends Position {
    override def stringRepresentation: String = col.toString
  }
  final case class RowColPosition(row: Row, col: Col) extends Position {
    override def stringRepresentation: String = s"$row${col.toString}"
  }

  final case class Range(start: Position, end: Position) {
    def stringRepresentation: String = s"${start.stringRepresentation}:${end.stringRepresentation}"
  }

  sealed trait A1Notation {
    def stringRepresentation: String
  }
  final case class SheetNameNotation(sheetName: String) extends A1Notation {
    override def stringRepresentation: String = sheetName
  }
  final case class RangeNotation(range: Range) extends A1Notation {
    override def stringRepresentation: String = range.stringRepresentation
  }
  final case class SheetNameRangeNotation(sheetName: String, range: Range) extends A1Notation {
    override def stringRepresentation: String = s"$sheetName!${range.stringRepresentation}"
  }

  implicit val a1NotationEncoder: Encoder[A1Notation] =
    Encoder.encodeString.contramap(_.stringRepresentation)

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
