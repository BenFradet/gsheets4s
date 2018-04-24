package gsheets4s
package unit

import org.scalacheck._

import arbitraries._
import interpreters._
import model._

object SpreadsheetsValuesSpec extends Properties("SpreadsheetsValues") {
  property("update/get program") = Prop.forAll { (spreadsheetId: String, vr: ValueRange) =>
    val (uvr, vr2) = new TestPrograms(TestSpreadsheetsValues).updateAndGet(spreadsheetId, vr)
    uvr.spreadsheetId == spreadsheetId &&
      uvr.updatedRange == vr.range &&
      vr.range == vr2.range &&
      vr.values == vr2.values
  }
}
