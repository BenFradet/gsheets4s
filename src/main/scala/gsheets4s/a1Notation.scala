package gsheets4s

import eu.timepit.refined._
import eu.timepit.refined.char._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._
import eu.timepit.refined.string._

object a1Notation {
  type SheetName = MatchesRegex[W.`"'.*'"`.T]

  type Row = Forall[UpperCase]
  type Col = Positive

  final case class Range(
    startRow: Row,
    startCol: Col,
    endRow: Row,
    endCol: Col
  )

  final case class A1Notation(
    sheetName: Option[SheetName],
    range: Option[Range]
  )
}
