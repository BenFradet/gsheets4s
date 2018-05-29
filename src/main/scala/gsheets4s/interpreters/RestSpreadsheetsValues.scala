package gsheets4s
package interpreters

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.either._
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
    creds: Ref[IO, Credentials])(implicit interpreter: Interpreter[IO])
      extends SpreadsheetsValues[IO] {

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

  private val accessTokenDecoder = new Decoder[String] {
    override def decode(entity: Entity): Either[CodecException, String] = entity match {
      case Entity.StringEntity(str, _) =>
        val decoder = io.circe.Decoder.decodeString.prepare(_.downField("access_token"))
        io.circe.parser.decode(str)(decoder)
          .leftMap(err => CodecException.withMessageAndException(err.getMessage, err))
      case _: Entity.ByteArrayEntity => CodecException
        .withMessage("unable to decode a ByteArrayEntity. Only StringEntity is supported").asLeft
      case Entity.EmptyEntity => CodecException
        .withMessage("unable to decode an EmptyEntity. Only StringEntity is supported").asLeft
    }
  }

  override def get(spreadsheetID: String, range: A1Notation): IO[Either[GsheetsError, ValueRange]] =
    requestWithToken(uri(spreadsheetID, range),
      request[Either[GsheetsError, ValueRange]](Method.GET, _))

  override def update(
    spreadsheetID: String,
    range: A1Notation,
    updates: ValueRange,
    valueInputOption: ValueInputOption
  ): IO[Either[GsheetsError, UpdateValuesResponse]] = {
    val u = uri(spreadsheetID, range)
      .andThen(_.param("valueInputOption", valueInputOption.value))
    requestWithToken(u,
      requestWithBody[ValueRange, Either[GsheetsError, UpdateValuesResponse]](Method.PUT, _, updates))
  }

  private def request[O](m: Method, uri: Uri)(implicit d: Decoder[O]): IO[O] =
    Hammock.request(m, uri, Map.empty).as[O].exec[IO]

  private def requestWithBody[I, O](m: Method, uri: Uri, body: I)(
      implicit c: Codec[I], d: Decoder[O]): IO[O] =
    Hammock.request(m, uri, Map.empty, Some(body)).as[O].exec[IO]

  private def requestWithToken[A](
    uriBuilder: String => Uri,
    ioBuilder: Uri => IO[Either[GsheetsError, A]]
  )(implicit d: Decoder[A]): IO[Either[GsheetsError, A]] = for {
    creds <- creds.get
    builder = ioBuilder compose uriBuilder
    either <- builder(creds.accessToken)
    retried <- either match {
      case Left(GsheetsError(401, _, _)) => requestWithNewToken(builder)
      case o => IO.pure(o)
    }
  } yield retried

  private def requestWithNewToken[A](builder: String => IO[Either[GsheetsError, A]])(
      implicit d: Decoder[A]): IO[Either[GsheetsError, A]] =
    for {
      c <- creds.get
      newAccessToken <- request[String](Method.POST, refreshTokenUri(c))(accessTokenDecoder)
      _ <- creds.setAsync(c.copy(accessToken = newAccessToken))
      r <- builder(newAccessToken)
    } yield r

}

object RestSpreadsheetsValues {
  def apply(creds: Ref[IO, Credentials])(
      implicit interpreter: Interpreter[IO]): RestSpreadsheetsValues =
    new RestSpreadsheetsValues(creds)
}
