package gsheets4s
package interpreters

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.option._
import cats.syntax.show._
import fs2.async.Ref
import hammock._
import hammock.marshalling._
import hammock.jvm.Interpreter
import hammock.circe.implicits._
import io.circe.generic.auto._

import algebras._
import model._

class RestSpreadsheetsValues private(
    creds: Credentials)(implicit interpreter: Interpreter[IO]) extends SpreadsheetsValues[IO] {

  private val accessTokenRef: IO[Ref[IO, String]] = Ref(creds.accessToken)

  private val uri = (id: String, range: A1Notation) => (accessToken: String) =>
    (Uri("https".some, none, "sheets.googleapis.com/v4/spreadsheets") /
      id / "values" / range.show).param("access_token", accessToken)

  private val refreshTokenUri = (creds: Credentials) =>
    Uri("https".some, none, "www.googleapis.com/oauth2/v4/token") ?
      NonEmptyList(
        ("refresh_token" -> creds.refreshToken),
        List(
          ("client_id" -> creds.clientId),
          ("client_secret" -> creds.clientSecret),
          ("grant_type" -> "refresh_token")
        )
      )

  def get(spreadsheetID: String, range: A1Notation): IO[Either[Error, ValueRange]] =
    requestWithToken(uri(spreadsheetID, range), request[Either[Error, ValueRange]](Method.GET, _))

  def update(
    spreadsheetID: String,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): IO[Either[Error, UpdateValuesResponse]] = {
    val u = uri(spreadsheetID, range)
      .andThen(_.param("valueInputOption", valueInputOption.value))
    requestWithToken(u,
      requestWithBody[ValueRange, Either[Error, UpdateValuesResponse]](Method.PUT, _, updates))
  }

  private def request[O](m: Method, uri: Uri)(implicit d: Decoder[O]): IO[O] =
    Hammock.request(m, uri, Map.empty).as[O].exec[IO]

  private def requestWithBody[I, O](m: Method, uri: Uri, body: I)(
      implicit c: Codec[I], d: Decoder[O]): IO[O] =
    Hammock.request(m, uri, Map.empty, Some(body)).as[O].exec[IO]

  private def requestWithToken[A](
    uriBuilder: String => Uri,
    ioBuilder: Uri => IO[Either[Error, A]]
  )(implicit d: Decoder[A]): IO[Either[Error, A]] = for {
    ref <- accessTokenRef
    token <- ref.get
    builder = ioBuilder compose uriBuilder
    either <- builder(token)
    retried <- either match {
      case Left(Error(401, _, _)) => requestWithNewToken(builder)
      case o => IO.pure(o)
    }
  } yield retried

  private def requestWithNewToken[A](
      builder: String => IO[Either[Error, A]])(implicit d: Decoder[A]): IO[Either[Error, A]] =
    for {
      ref <- accessTokenRef
      newToken <- request[AccessToken](Method.POST, refreshTokenUri(creds))
      _ <- ref.setAsync(newToken.access_token)
      r <- builder(newToken.access_token)
    } yield r

}

object RestSpreadsheetsValues {
  def apply(creds: Credentials)(implicit interpreter: Interpreter[IO]): RestSpreadsheetsValues =
    new RestSpreadsheetsValues(creds)
}
