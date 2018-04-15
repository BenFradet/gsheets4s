package gsheets4s

import atto.Atto._

import eu.timepit.refined.scalacheck.any._
import org.scalacheck._

import model._

object ModelSpec extends Properties("model") {
  import Prop._

  implicit def arbRow: Arbitrary[Row] = arbitraryFromValidate
  implicit def arbCol: Arbitrary[Col] = {
    val gen: Gen[String] = Gen.nonEmptyListOf(Gen.choose('A', 'Z')).map(_.mkString)
    arbitraryFromValidate(implicitly, implicitly, Arbitrary(gen))
  }

  property("Position parser") = forAll { (c: Col, r: Row) =>
    Position.parser.parseOnly(c.toString).option == Some(ColPosition(c)) &&
    Position.parser.parseOnly(r.toString).option == Some(RowPosition(r)) &&
    Position.parser.parseOnly(c.toString + r.toString).option == Some(ColRowPosition(c, r))
  }
}
