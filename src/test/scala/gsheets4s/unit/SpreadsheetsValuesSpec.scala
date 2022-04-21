package gsheets4s
package unit

import org.scalacheck._
import eu.timepit.refined.types.string.NonEmptyString
import eu.timepit.refined.scalacheck.string._

import arbitraries._
import interpreters._
import model._

object SpreadsheetsValuesSpec extends Properties("SpreadsheetsValues unit") {
  property("update/get program") = Prop.forAll { (id: NonEmptyString, vr: ValueRange, vio: ValueInputOption) =>
    new TestPrograms(new TestSpreadsheetsValues).updateAndGet(id, vr, vio).map { case (uvr, vr2) =>
      uvr.spreadsheetId == id.value &&
        uvr.updatedRange == vr.range &&
        vr.range == vr2.range &&
        vr.values == vr2.values
    }.getOrElse(false)
  }

  property("append/get program") = Prop.forAll { (id: NonEmptyString, vr: ValueRange, vio: ValueInputOption) =>
    new TestPrograms(new TestSpreadsheetsValues).appendAndGet(id, vr, vio).map { case (avr, vr2) =>
      avr.spreadsheetId == id.value &&
        avr.updates.updatedRange == vr.range &&
        vr.range == vr2.range &&
        vr.values == vr2.values
    }.getOrElse(false)
  }
}
