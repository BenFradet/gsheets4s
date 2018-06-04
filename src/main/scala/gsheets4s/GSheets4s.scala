package gsheets4s

import cats.effect.IO
import fs2.async
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

  def apply(creds: Credentials): IO[GSheets4s] = for {
    credsRef <- async.refOf[IO, Credentials](creds)
    requester = new HammockRequester[IO]()
    client = new HttpClient[IO](credsRef, requester)
    spreadsheetsValues = new RestSpreadsheetsValues(client)
  } yield GSheets4s(spreadsheetsValues)
}
