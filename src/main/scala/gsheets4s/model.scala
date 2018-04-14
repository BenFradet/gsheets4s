package gsheets4s

import atto._
import atto.Atto._
import atto.syntax.refined._
import cats.Show
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.show._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean._
import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}

object model {
  type ValidCol = NonEmpty And Forall[UpperCase]
  type Col = String Refined ValidCol
  type ValidRow = Positive
  type Row = Int Refined ValidRow

  sealed trait Position
  object Position {
    implicit val showPosition: Show[Position] = Show.show {
      case ColPosition(c) => c.toString
      case RowPosition(r) => r.toString
      case ColRowPosition(c, r) => s"$c${r.toString}"
    }
    val parser: Parser[Position] = {
      val colP = stringOf1(upper).refined[ValidCol].namedOpaque("col")
      val rowP = int.refined[ValidRow].namedOpaque("row")
      (colP, rowP).mapN(ColRowPosition(_, _): Position) |
        colP.map(ColPosition(_): Position) |
        rowP.map(RowPosition(_): Position)
    }
  }
  final case class ColPosition(col: Col) extends Position
  final case class RowPosition(row: Row) extends Position
  final case class ColRowPosition(col: Col, row: Row) extends Position

  final case class Range(start: Position, end: Position)
  object Range {
    implicit val showRange: Show[Range] = Show.show(r => s"${r.start}:${r.end}")
    val parser: Parser[Range] = (Position.parser <~ Atto.char(':'), Position.parser).mapN(Range.apply)
  }

  sealed trait A1Notation
  object A1Notation {
    implicit val showA1Notation: Show[A1Notation] = Show.show {
      case SheetNameNotation(s) => s
      case RangeNotation(r) => r.show
      case SheetNameRangeNotation(s, r) => s"$s!$r"
    }
    val parser: Parser[A1Notation] =
      (takeWhile(_ != '!') <~ Atto.char('!'), Range.parser).mapN(SheetNameRangeNotation.apply) |
        Range.parser.map(RangeNotation(_): A1Notation) |
        stringOf1(elem(_ => true)).map(SheetNameNotation(_): A1Notation)
  }
  final case class SheetNameNotation(sheetName: String) extends A1Notation
  final case class RangeNotation(range: Range) extends A1Notation
  final case class SheetNameRangeNotation(sheetName: String, range: Range) extends A1Notation
  implicit val a1NotationDecoder: Decoder[A1Notation] = Decoder.decodeString.flatMap { s =>
    new Decoder[A1Notation] {
      final def apply(c: HCursor): Decoder.Result[A1Notation] =
        A1Notation.parser.parseOnly(s).either
          .leftMap(DecodingFailure(_, c.history))
    }
  }
  implicit val a1NotationEncoder: Encoder[A1Notation] =
    Encoder.encodeString.contramap(_.show)

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
