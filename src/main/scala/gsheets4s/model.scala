package gsheets4s

import scala.util.Try

import cats.syntax.either._
import eu.timepit.refined._
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
  final case class ColPosition(col: Col) extends Position {
    override def stringRepresentation: String = col.toString
  }
  object ColPosition {
    def parse(s: String): Either[String, ColPosition] = refineV[ValidCol](s).map(ColPosition(_))
  }
  final case class RowPosition(row: Row) extends Position {
    override def stringRepresentation: String = row.toString
  }
  object RowPosition {
    def parse(s: String): Either[String, RowPosition] =
      for {
        int <- Try(s.toInt).toEither.leftMap(_.getMessage)
        col <- refineV[ValidRow](int)
        rowPos = RowPosition(col)
      } yield rowPos
  }
  final case class ColRowPosition(col: Col, row: Row) extends Position {
    override def stringRepresentation: String = s"$col${row.toString}"
  }
  object ColRowPosition {
    def parse(s: String): Either[String, ColRowPosition] = {
      val (chars, digits) = s.foldLeft((List.empty[Char], List.empty[Char])) { case ((cs, ds), c) =>
        if (c.isDigit) (cs, c :: ds)
        else (c :: cs, ds)
      }
      for {
        int <- Try(digits.reverse.mkString.toInt).toEither.leftMap(_.getMessage)
        row <- refineV[ValidRow](int)
        str = chars.reverse.mkString
        col <- refineV[ValidCol](str)
        colRow = ColRowPosition(col, row)
      } yield colRow
    }
  }

  final case class Range(start: Position, end: Position) {
    def stringRepresentation: String = s"${start.stringRepresentation}:${end.stringRepresentation}"
  }
  object Range {
    def parse(s: String): Either[String, Range] = {
      val splits = s.split(":")
      if (splits.length == 2) {
        val Array(start, end) = splits
        val startPos = ColPosition.parse(start) orElse RowPosition.parse(start) orElse
          ColRowPosition.parse(start)
        val endPos = ColPosition.parse(end) orElse RowPosition.parse(end) orElse
          ColRowPosition.parse(end)
        for {
          startP <- startPos
          endP <- endPos
        } yield Range(startP, endP)
      } else {
        Left("Invalid range, must be in the colrow:colrow format")
      }
    }
  }

  sealed trait A1Notation {
    def stringRepresentation: String
  }
  final case class SheetNameNotation(sheetName: String) extends A1Notation {
    override def stringRepresentation: String = sheetName
  }
  object SheetNameNotation {
    def parse(s: String): Either[String, SheetNameNotation] = Right(SheetNameNotation(s))
  }
  final case class RangeNotation(range: Range) extends A1Notation {
    override def stringRepresentation: String = range.stringRepresentation
  }
  object RangeNotation {
    def parse(s: String): Either[String, RangeNotation] = Range.parse(s).map(RangeNotation(_))
  }
  final case class SheetNameRangeNotation(sheetName: String, range: Range) extends A1Notation {
    override def stringRepresentation: String = s"$sheetName!${range.stringRepresentation}"
  }
  object SheetNameRangeNotation {
    def parse(s: String): Either[String, SheetNameRangeNotation] = {
      val splits = s.split("!")
      if (splits.length == 2) {
        val Array(sheetName, range) = splits
        Range.parse(range).map(SheetNameRangeNotation(sheetName, _))
      } else {
        Left("Invalid notation, must be in the sheetName!range format")
      }
    }
  }
  implicit val a1NotationDecoder: Decoder[A1Notation] = Decoder.decodeString.flatMap { s =>
    new Decoder[A1Notation] {
      final def apply(c: HCursor): Decoder.Result[A1Notation] =
        (SheetNameNotation.parse(s) orElse RangeNotation.parse(s) orElse SheetNameNotation.parse(s))
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
