package gsheets4s

import eu.timepit.refined.scalacheck.any._
import org.scalacheck.{Arbitrary, Gen}

import model._

object arbitraries {
  implicit def arbRow: Arbitrary[Row] = arbitraryFromValidate
  implicit def arbCol: Arbitrary[Col] = {
    val gen: Gen[String] = Gen.nonEmptyListOf(Gen.choose('A', 'Z')).map(_.mkString)
    arbitraryFromValidate(implicitly, implicitly, Arbitrary(gen))
  }

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

  implicit def arbSheetName: Arbitrary[SheetName] = arbitraryFromValidate

  implicit def arbA1Notation: Arbitrary[A1Notation] =
    Arbitrary {
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
}
