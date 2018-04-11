package gsheets4s

import atto._
import atto.Atto._
import atto.syntax.refined._
import cats.implicits._
import cats.syntax.either._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}

object model {
  type ValidRow = Positive
  type Row = Int Refined ValidRow
  type ValidCol = Forall[UpperCase]
  type Col = String Refined ValidCol

  sealed trait Position {
    def stringRepresentation: String
  }
  object Position {
    val parser: Parser[Position] = {
      val colP = stringOf1(upper).refined[ValidCol]
      val rowP = int.refined[ValidRow]
      colP.map(ColPosition(_): Position) |
        rowP.map(RowPosition(_): Position) |
        (colP, rowP).mapN(ColRowPosition(_, _): Position)
    }
  }
  final case class ColPosition(col: Col) extends Position {
    override def stringRepresentation: String = col.toString
  }
  final case class RowPosition(row: Row) extends Position {
    override def stringRepresentation: String = row.toString
  }
  final case class ColRowPosition(col: Col, row: Row) extends Position {
    override def stringRepresentation: String = s"$col${row.toString}"
  }

  final case class Range(start: Position, end: Position) {
    def stringRepresentation: String = s"${start.stringRepresentation}:${end.stringRepresentation}"
  }
  object Range {
    val parser: Parser[Range] = (Position.parser <~ Atto.char(':'), Position.parser).mapN(Range.apply)
  }

  sealed trait A1Notation {
    def stringRepresentation: String
  }
  object A1Notation {
    val parser: Parser[A1Notation] =
      (takeWhile(_ != '!') <~ Atto.char('!'), Range.parser).mapN(SheetNameRangeNotation.apply) |
        Range.parser.map(RangeNotation(_): A1Notation) |
        stringOf1(elem(_ => true)).map(SheetNameNotation(_): A1Notation)
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
  implicit val a1NotationDecoder: Decoder[A1Notation] = Decoder.decodeString.flatMap { s =>
    new Decoder[A1Notation] {
      final def apply(c: HCursor): Decoder.Result[A1Notation] =
        A1Notation.parser.parseOnly(s).either
          .leftMap(DecodingFailure(_, c.history))
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
