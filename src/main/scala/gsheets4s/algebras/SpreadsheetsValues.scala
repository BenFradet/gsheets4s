package gsheets4s.algebras

import gsheets4s.a1Notation._

trait SpreadsheetsValues[F[_]] {
  def get(spreadsheetID: String, range: A1Notation): F[Map[String, String]]
}
