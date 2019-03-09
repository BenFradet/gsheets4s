package gsheets4s

import cats.Monad
import cats.data.EitherT

import algebras._
import model._

class TestPrograms[F[_]: Monad](alg: SpreadsheetsValues[F]) {
  def updateAndGet(
    spreadsheetId: String,
    vr: ValueRange,
    vio: ValueInputOption
  ): F[Either[GsheetsError, (UpdateValuesResponse, ValueRange)]] =
    (for {
      updateValuesResponse <- EitherT(alg.update(spreadsheetId, vr.range, vr, vio))
      valueRange <- EitherT(alg.get(spreadsheetId, vr.range))
    } yield (updateValuesResponse, valueRange)).value

  def get(
    spreadsheetId: String,
    not: A1Notation,
  ): F[Either[GsheetsError, ValueRange]] =
    alg.get(spreadsheetId, not)
}
