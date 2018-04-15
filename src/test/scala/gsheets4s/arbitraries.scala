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

  implicit def arbPosition: Arbitrary[Position] = {
    val gen: Gen[Position] = for {
      col <- arbCol.arbitrary
      row <- arbRow.arbitrary
      pos <- Gen.oneOf(ColPosition(col): Position, RowPosition(row), ColRowPosition(col, row))
    } yield pos
    Arbitrary(gen)
  }
}
