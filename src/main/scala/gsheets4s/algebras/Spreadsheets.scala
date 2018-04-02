package gsheets4s

trait Spreadsheets[F[_]] {
  def get(spreadsheetID: String): F[String]
}
