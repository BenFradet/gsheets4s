package gsheets4s
package unit

import cats.syntax.flatMap._
import cats.syntax.functor._
import org.scalacheck._

import arbitraries._
import interpreters._
import model._

object SpreadsheetsValuesSpec extends Properties("SpreadsheetsValues") {
  property("update/get program") = Prop.forAll { (spreadsheetId: String, vr: ValueRange) =>
    val (uvr, vr2) = for {
      updateValuesResponse <- TestSpreadsheetsValues.update(spreadsheetId, vr.range, vr)
      valueRange <- TestSpreadsheetsValues.get(spreadsheetId, vr.range)
    } yield (updateValuesResponse, valueRange)
    uvr.spreadsheetId == spreadsheetId &&
      uvr.updatedRange == vr.range &&
      vr.range == vr2.range &&
      vr.values == vr2.values
  }
}
