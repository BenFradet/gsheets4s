package gsheets4s

import cats.Monad
import cats.data.EitherT
import eu.timepit.refined.types.string.NonEmptyString

import algebras._
import model._

class TestPrograms[F[_]: Monad](alg: SpreadsheetsValues[F]) {
  import alg._

  def updateAndGet(
    spreadsheetId: NonEmptyString,
    vr: ValueRange,
    vio: ValueInputOption
  ): F[Either[GsheetsError, (UpdateValuesResponse, ValueRange)]] =
    (for {
      updateValuesResponse <- EitherT(update(spreadsheetId, vr.range, vr, vio))
      valueRange <- EitherT(get(spreadsheetId, vr.range))
    } yield (updateValuesResponse, valueRange)).value
}
