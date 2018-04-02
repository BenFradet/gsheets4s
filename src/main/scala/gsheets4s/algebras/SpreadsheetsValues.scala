package gsheets4s

trait SpreadsheetsValues[F[_]] {
  def get(spreadsheetID: String, range: String): F[Map[String, String]]
}
