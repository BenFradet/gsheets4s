package gsheets4s

import eu.timepit.refined.auto._
import gsheets4s.model._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class A1NotationLiteralsSpec extends AnyFlatSpec with Matchers {

  behavior of "A1Notation literals"

  it should "parse an A1 notation literal" in {
    a1"Sheet1!A1:B2" should be(SheetNameRangeNotation("Sheet1", Range(ColRowPosition("A", 1), ColRowPosition("B", 2))))
  }

}
