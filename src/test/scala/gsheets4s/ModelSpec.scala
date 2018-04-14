package gsheets4s

import atto.Atto._

import eu.timepit.refined.scalacheck.any._
import org.scalacheck._

import model._

object ModelSpec extends Properties("model") {
  import Prop._

  implicit def arbRow: Arbitrary[Row] = arbitraryFromValidate
  val nonEmptyUpperStrGen: Gen[String] =
    Gen.nonEmptyListOf(Gen.choose('A', 'Z')).map(_.mkString)
  implicit def arbCol: Arbitrary[Col] =
    arbitraryFromValidate(implicitly, implicitly, Arbitrary(nonEmptyUpperStrGen))

  property("ColPosition parser") = forAll { c: Col =>
    Position.parser.parseOnly(c.toString).option == Some(ColPosition(c))
  }

  property("RowPosition parser") = forAll { (r: Row) =>
    Position.parser.parseOnly(r.toString).option == Some(RowPosition(r))
  }

  property("ColRowPosition parser") = forAll { (c: Col, r: Row) =>
    Position.parser.parseOnly(c.toString + r.toString).option == Some(ColRowPosition(c, r))
  }
}
