package gsheets4s

import cats.implicits._
import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import io.circe.{Decoder, Encoder}

object model {
  type Row = Forall[UpperCase]
  type Col = Positive

  final case class Position(row: Option[Row], col: Option[Col]) {
    def stringRepresentation: String = row.getOrElse("") + col.foldMap(_.toString)
  }

  final case class Range(start: Position, end: Position) {
    def stringRepresentation: String = start.stringRepresentation + ":" + end.stringRepresentation
  }

  final case class A1Notation(
    sheetName: Option[String],
    range: Option[Range]
  ) {
    def stringRepresentation: String = (sheetName, range) match {
      case (Some(n), Some(r)) => s"$n!${r.stringRepresentation}"
      case (Some(sheetName), None) => sheetName
      case (None, Some(range)) => range.stringRepresentation
      // shortcoming
      case _ => ""
    }
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
