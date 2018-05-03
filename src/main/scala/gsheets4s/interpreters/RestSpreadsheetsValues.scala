package gsheets4s
package interpreters

import cats.effect.IO
import cats.syntax.show._
import cats.syntax.option._
import fs2.async.Ref
import hammock._
import hammock.marshalling._
import hammock.jvm.Interpreter
import hammock.circe.implicits._
import io.circe.generic.auto._

import algebras._
import model._

class RestSpreadsheetsValues private(
    accessToken: String)(implicit interpreter: Interpreter[IO]) extends SpreadsheetsValues[IO] {

  private val accessTokenRef: IO[Ref[IO, String]] = Ref(accessToken)

  private val uri = (id: String, range: A1Notation) => (accessToken: String) =>
    (Uri("https".some, none, "sheets.googleapis.com/v4/spreadsheets") /
      id / "values" / range.show).param("access_token", accessToken)

  def get(spreadsheetID: String, range: A1Notation): IO[Either[Error, ValueRange]] =
    requestWithToken(uri(spreadsheetID, range), get[Either[Error, ValueRange]](_))

  def update(
    spreadsheetID: String,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): IO[Either[Error, UpdateValuesResponse]] = {
    val u = uri(spreadsheetID, range)
      .andThen(_.param("valueInputOption", valueInputOption.value))
    requestWithToken(u, put[ValueRange, Either[Error, UpdateValuesResponse]](_, updates))
  }

  private def get[O](uri: Uri)(implicit d: Decoder[O]): IO[O] = Hammock
    .request(Method.GET, uri, Map.empty)
    .as[O]
    .exec[IO]

  private def put[I, O](uri: Uri, body: I)(implicit c: Codec[I], d: Decoder[O]): IO[O] = Hammock
    .request(Method.PUT, uri, Map.empty, Some(body))
    .as[O]
    .exec[IO]

  private def requestWithToken[A](uriBuilder: String => Uri, ioBuilder: Uri => IO[A]): IO[A] = for {
    ref <- accessTokenRef
    token <- ref.get
    uri = uriBuilder(token)
    io <- ioBuilder(uri)
  } yield io

  // get new token
  // set new token
  // rerun
  private def retryWithNewToken[A](
      io: IO[Either[Error, A]])(implicit d: Decoder[A]): IO[Either[Error, A]] = for {
        ref <- accessTokenRef
        _ <- ref.setSync("abcdef")
        r <- io
    } yield r
}

object RestSpreadsheetsValues {
  def apply(accessToken: String)(implicit interpreter: Interpreter[IO]): RestSpreadsheetsValues =
    new RestSpreadsheetsValues(accessToken)
}
