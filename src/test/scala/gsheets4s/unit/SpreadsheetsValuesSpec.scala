package gsheets4s
package unit

import org.scalacheck._

import arbitraries._
import interpreters._
import model._

object SpreadsheetsValuesSpec extends Properties("SpreadsheetsValues unit") {
  property("update/get program") = Prop.forAll { (id: String, vr: ValueRange, vio: ValueInputOption) =>
    new TestPrograms(TestSpreadsheetsValues).updateAndGet(id, vr, vio).map { case (uvr, vr2) =>
      uvr.spreadsheetId == id &&
        uvr.updatedRange == vr.range &&
        vr.range == vr2.range &&
        vr.values == vr2.values
    }.getOrElse(false)
  }
}
