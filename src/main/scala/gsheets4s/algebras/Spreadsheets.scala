package gsheets4s.algebras

trait Spreadsheets[F[_]] {
  def get(spreadsheetID: String): F[String]
}
