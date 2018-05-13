package gsheets4s.http

import cats.syntax.option._
import hammock.Uri

case class GSheets4sDefaultUrls(
  baseUrl: Uri,
  refreshTokenUrl: Uri
)

object GSheets4sDefaultUrls {
  implicit val defaultUrls: GSheets4sDefaultUrls = GSheets4sDefaultUrls(
    Uri("https".some, none, "sheets.googleapis.com/v4/spreadsheets"),
    Uri("https".some, none, "www.googleapis.com/oauth2/v4/token")
  )
}
