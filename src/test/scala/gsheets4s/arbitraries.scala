package gsheets4s

import eu.timepit.refined.scalacheck.any._
import org.scalacheck.{Arbitrary, Gen}

import model._

object arbitraries {
  private val nonEmptyUpperChars: Gen[String] =
    Gen.nonEmptyListOf(Gen.choose('A', 'Z')).map(_.mkString)

  implicit def arbRow: Arbitrary[Row] = arbitraryFromValidate
  implicit def arbCol: Arbitrary[Col] =
    arbitraryFromValidate(implicitly, implicitly, Arbitrary(nonEmptyUpperChars))

  implicit def arbPosition: Arbitrary[Position] = Arbitrary {
    for {
      col <- arbCol.arbitrary
      row <- arbRow.arbitrary
      pos <- Gen.oneOf(ColPosition(col): Position, RowPosition(row), ColRowPosition(col, row))
    } yield pos
  }

  implicit def arbRange: Arbitrary[Range] = Arbitrary {
    for {
      start <- arbPosition.arbitrary
      end <- arbPosition.arbitrary
    } yield Range(start, end)
  }

  implicit def arbSheetName: Arbitrary[SheetName] =
    arbitraryFromValidate(implicitly, implicitly, Arbitrary(nonEmptyUpperChars))

  implicit def arbA1Notation: Arbitrary[A1Notation] = Arbitrary {
    for {
      sheetName <- arbSheetName.arbitrary
      range <- arbRange.arbitrary
      notation <- Gen.oneOf(
        SheetNameNotation(sheetName),
        RangeNotation(range),
        SheetNameRangeNotation(sheetName, range)
      )
    } yield notation
  }

  implicit def arbDimension: Arbitrary[Dimension] = Arbitrary { Gen.oneOf(Rows, Columns) }

  implicit def arbValueRange: Arbitrary[ValueRange] = Arbitrary {
    for {
      notation <- arbA1Notation.arbitrary
      dim <- arbDimension.arbitrary
      values <- Gen.listOfN(10, Gen.listOfN(10, nonEmptyUpperChars))
    } yield ValueRange(notation, dim, values)
  }
}
