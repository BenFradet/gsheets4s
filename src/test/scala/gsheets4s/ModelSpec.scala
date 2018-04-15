package gsheets4s

import atto.Atto._
import cats.syntax.show._
import org.scalacheck._

import arbitraries._
import model._

object ModelSpec extends Properties("model") {
  import Prop._

  property("Position roundtrip") = forAll { p: Position =>
    Position.parser.parseOnly(p.show).option == Some(p)
  }

  property("Range roundtrip") = forAll { r: Range =>
    Range.parser.parseOnly(r.show).option == Some(r)
  }

  property("A1Notation roundtrip") = forAll { n: A1Notation =>
    A1Notation.parser.parseOnly(n.show).option == Some(n)
  }
}
