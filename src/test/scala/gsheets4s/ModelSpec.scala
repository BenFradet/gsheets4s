package gsheets4s

import atto.Atto._
import cats.syntax.show._
import org.scalacheck._

import arbitraries._
import model._

object ModelSpec extends Properties("model") {
  import Prop._

  property("Position parser") = forAll { (p: Position) =>
    Position.parser.parseOnly(p.show).option == Some(p)
  }
}
