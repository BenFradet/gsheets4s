package gsheets4s
package http

import cats.Monad
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.show._
import hammock._
import hammock.apache.ApacheInterpreter
import hammock.circe._
import io.circe.{Encoder, Decoder}
import io.lemonlabs.uri.Url

import model.{Credentials, GsheetsError}

trait HttpRequester[F[_]] {
  def request[O](uri: Uri, method: Method)(implicit d: Decoder[O]): F[O]
  def requestWithBody[I, O](
    uri: Uri, body: I, method: Method)(implicit e: Encoder[I], d: Decoder[O]): F[O]
}

class HammockRequester[F[_]: Sync] extends HttpRequester[F] {
  implicit val interpreter = ApacheInterpreter.instance[F]

  def request[O](uri: Uri, method: Method)(implicit d: Decoder[O]): F[O] = {
    implicit val hammockDecoder = new HammockDecoderForCirce()
    Hammock.request(method, uri, Map.empty).as[O].exec[F]
  }

  def requestWithBody[I, O](
    uri: Uri, body: I, method: Method)(implicit e: Encoder[I], d: Decoder[O]): F[O] = {
      implicit val hammockEncoder = new HammockEncoderForCirce()
      implicit val hammockDecoder = new HammockDecoderForCirce()
      Hammock.request(method, uri, Map.empty, Some(body)).as[O].exec[F]
    }
}

class HttpClient[F[_]: Monad](creds: Ref[F, Credentials], requester: HttpRequester[F])(
    implicit urls: GSheets4sDefaultUrls) {
  def get[O](
    path: Url,
    params: List[(String, String)] = List.empty)(
    implicit d: Decoder[O]): F[Either[GsheetsError, O]] =
      req(token => requester
        .request[Either[GsheetsError, O]](urlBuilder(token, path, params), Method.GET))

  def put[I, O](
    path: Url,
    body: I,
    params: List[(String, String)] = List.empty)(
    implicit e: Encoder[I], d: Decoder[O]): F[Either[GsheetsError, O]] =
      req(token => requester.requestWithBody[I, Either[GsheetsError, O]](
        urlBuilder(token, path, params), body, Method.PUT))

  def post[I, O](
    path: Url,
    body: I,
    params: List[(String, String)] = List.empty)(
    implicit e: Encoder[I], d: Decoder[O]): F[Either[GsheetsError, O]] =
      req(token => requester.requestWithBody[I, Either[GsheetsError, O]](
        urlBuilder(token, path, params), body, Method.POST))

  private def req[O](req: String => F[Either[GsheetsError, O]]): F[Either[GsheetsError, O]] = for {
    c <- creds.get
    first <- req(c.accessToken)
    retried <- first match {
      case Left(GsheetsError(401, _, _)) => reqWithNewToken(req, c)
      case o => Monad[F].pure(o)
    }
  } yield retried

  private def reqWithNewToken[O](
    req: String => F[Either[GsheetsError, O]], c: Credentials): F[Either[GsheetsError, O]] = for {
      newToken <- refreshToken(c)(Decoder.decodeString.prepare(_.downField("access_token")))
      _ <- creds.set(c.copy(accessToken = newToken))
      r <- req(newToken)
    } yield r

  private def refreshToken(c: Credentials)(implicit d: Decoder[String]): F[String] = {
    val url = urls.refreshTokenUrl ?
      NonEmptyList(
        ("refresh_token" -> c.refreshToken),
        List(
          ("client_id" -> c.clientId),
          ("client_secret" -> c.clientSecret),
          ("grant_type" -> "refresh_token")
        )
      )
    requester.request(url, Method.POST)
  }

  private def urlBuilder(
    accessToken: String,
    path: Url,
    params: List[(String, String)]): Uri =
      (urls.baseUrl / path.show) ? NonEmptyList(("access_token" -> accessToken), params)
}
