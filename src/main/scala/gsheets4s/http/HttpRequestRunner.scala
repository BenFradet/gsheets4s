package gsheets4s.http

import io.circe.{Encoder, Decoder}

trait HttpRequester[F[_]] {
  def request[O]()(implicit d: Decoder[O]): F[O]
  def requestWithBody[I, O]()(implicit e: Encoder[I], d: Decoder[O]): F[O]
}
