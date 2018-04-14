package gsheets4s

import atto.Atto._

import eu.timepit.refined.scalacheck.any._
import org.scalacheck._

import model._

object ModelSpec extends Properties("model") {
  import Prop._

  val nonEmptyUpperStrGen: Gen[List[Char]] = Gen.nonEmptyListOf(Gen.choose('A', 'Z'))
  implicit def arbRow: Arbitrary[Row] = arbitraryFromValidate

  property("ColPosition parser") = forAll(nonEmptyUpperStrGen) { c =>
    Position.parser.parseOnly(c.mkString).option match {
      case Some(ColPosition(_)) => true
      case _ => false
    }
  }

  property("RowPosition parser") = forAll { (r: Row) =>
    Position.parser.parseOnly(r.toString).option == Some(RowPosition(r))
  }

  property("ColRowPosition parser") = forAll(nonEmptyUpperStrGen, arbRow.arbitrary) { (c, r) =>
    Position.parser.parseOnly(c.mkString + r.toString).either match {
      case Right(ColRowPosition(_, _)) => true
      case e => println(e); false
    }
  }
}
