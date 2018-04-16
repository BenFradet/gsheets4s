package gsheets4s

import atto.Atto._
import cats.syntax.show._
import io.circe.parser._
import io.circe.syntax._
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

  property("A1Notation encode/decoder") = forAll { n: A1Notation =>
    decode[A1Notation](n.asJson.noSpaces).isRight
  }

  property("Dimension encoder/decoder") = forAll { d: Dimension =>
    decode[Dimension](d.asJson.noSpaces).isRight
  }
}
