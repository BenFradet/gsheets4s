package gsheets4s

import cats.effect.Sync
import cats.effect.concurrent.Ref

import algebras._
import http._
import interpreters._
import model._

case class GSheets4s[F[_]](
  spreadsheetsValues: SpreadsheetsValues[F]
)

object GSheets4s {
  def apply[F[_]: Sync](creds: Ref[F, Credentials]): GSheets4s[F] = {
    val requester = new HammockRequester[F]()
    val client = new HttpClient[F](creds, requester)
    val spreadsheetsValues = new RestSpreadsheetsValues(client)
    GSheets4s(spreadsheetsValues)
  }
}
