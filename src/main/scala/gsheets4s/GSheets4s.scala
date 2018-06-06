package gsheets4s

import cats.effect.IO
import fs2.async.Ref
import hammock.jvm.Interpreter

import algebras._
import http._
import interpreters._
import model._

case class GSheets4s(
  spreadsheetsValues: SpreadsheetsValues[IO]
)

object GSheets4s {
  implicit val interpreter = Interpreter[IO]

  def apply(creds: Ref[IO, Credentials]): GSheets4s = {
    val requester = new HammockRequester[IO]()
    val client = new HttpClient[IO](creds, requester)
    val spreadsheetsValues = new RestSpreadsheetsValues(client)
    GSheets4s(spreadsheetsValues)
  }
}
