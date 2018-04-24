package gsheets4s

import cats.FlatMap
import cats.syntax.flatMap._
import cats.syntax.functor._

import algebras._
import model._

class TestPrograms[F[_]: FlatMap](alg: SpreadsheetsValues[F]) {
  import alg._

  def updateAndGet(spreadsheetId: String, vr: ValueRange): F[(UpdateValuesResponse, ValueRange)] =
    for {
      updateValuesResponse <- update(spreadsheetId, vr.range, vr)
      valueRange <- get(spreadsheetId, vr.range)
    } yield (updateValuesResponse, valueRange)
}
