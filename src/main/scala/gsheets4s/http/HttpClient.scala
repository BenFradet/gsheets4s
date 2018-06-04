package gsheets4s
package http

import cats.~>
import cats.Monad
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import hammock._
import hammock.circe._
import fs2.async.Ref
import io.circe.{Encoder, Decoder}

import model.{Credentials, GsheetsError}

trait HttpRequester[F[_]] {
  def request[O](uri: Uri, method: Method)(implicit d: Decoder[O]): F[O]
  def requestWithBody[I, O](
    uri: Uri, body: I, method: Method)(implicit e: Encoder[I], d: Decoder[O]): F[O]
}

class HammockRequester[F[_]: Sync](implicit nat: HammockF ~> F) extends HttpRequester[F] {
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

class HttpClient[F[_]: Monad](creds: Ref[F, Credentials])(
    implicit urls: GSheets4sDefaultUrls, requester: HttpRequester[F]) {
  def get[O](
    path: String,
    params: List[(String, String)] = List.empty)(
    implicit d: Decoder[O]): F[Either[GsheetsError, O]] = for {
      c <- creds.get
      res <- requester
        .request[Either[GsheetsError, O]](urlBuilder(c.accessToken, path, params), Method.GET)
    } yield res

  def put[I, O](
    path: String,
    body: I,
    params: List[(String, String)] = List.empty)(
    implicit e: Encoder[I], d: Decoder[O]): F[Either[GsheetsError, O]] = for {
      c <- creds.get
      res <- requester.requestWithBody[I, Either[GsheetsError, O]](
        urlBuilder(c.accessToken, path, params), body, Method.PUT)
    } yield res

  def refreshToken(creds: Credentials)(implicit d: Decoder[String]): F[String] = {
    val url = urls.refreshTokenUrl ?
      NonEmptyList(
        ("refresh_token" -> creds.refreshToken),
        List(
          ("client_id" -> creds.clientId),
          ("client_secret" -> creds.clientSecret),
          ("grant_type" -> "refresh_token")
        )
      )
    requester.request(url, Method.POST)
  }

  private def urlBuilder(
    accessToken: String,
    path: String,
    params: List[(String, String)] = List.empty): Uri =
      (urls.baseUrl / path) ? NonEmptyList(("access_token" -> accessToken), params)
}
