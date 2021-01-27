# gsheets4s

[![Build Status](https://travis-ci.org/BenFradet/gsheets4s.svg?branch=master)](https://travis-ci.org/BenFradet/gsheets4s)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.benfradet/gsheets4s_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.benfradet/gsheets4s_2.12)
[![codecov](https://codecov.io/gh/BenFradet/gsheets4s/branch/master/graph/badge.svg)](https://codecov.io/gh/BenFradet/gsheets4s)
[![Join the chat at https://gitter.im/BenFradet/gsheets4s](https://badges.gitter.im/BenFradet/gsheets4s.svg)](https://gitter.im/BenFradet/gsheets4s?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Scala wrapper around [the Google Sheets API](https://developers.google.com/sheets/api/reference/rest/)

## Installation

include the latest version of the library dependency in your build.sbt

`"com.itv" %% "ctp-gsheets4s" % "0.9.0"`

## Credentials management

gsheets4s uses OAuth 2.0 to authenticate requests made to Google servers, the process to get your OAuth 2.0
credentials is detailed at: https://developers.google.com/identity/protocols/OAuth2ForDevices.

Out of this process, gsheets4s needs:

- an initial access token (it will take care of refreshing it when needed)
- a refresh token
- a client ID
- a client secret

The last three are needed in order to refresh the access token as it is valid for only one hour.

## Usage

Here's a program you could write that updates and consecutively gets contents from a spreadhseet:

```scala
import cats.Monad
import cats.data.EitherT
import gsheets4s.algebras.SpreadsheetsValues
import gsheets4s.model._

class Program[F[_]: Monad](alg: SpreadsheetsValues[F]) {
  import alg._

  def updateAndGet(
    spreadsheetId: String,
    vr: ValueRange,
    vio: ValueInputOption
  ): F[Either[GsheetsError, (UpdateValuesResponse, ValueRange)]] =
    (for {
      updateValuesResponse <- EitherT(update(spreadsheetId, vr.range, vr, vio))
      valueRange <- EitherT(get(spreadsheetId, vr.range))
    } yield (updateValuesResponse, valueRange)).value
}
```

And here's how you could use it leveraging cats-effect's `Sync`:

```scala
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import eu.timepit.refined.auto._
import gsheets4s.GSheets4s
import gsheets4s.model._

class GSheetsService[F[_]: Sync](credentials: Ref[F, Credentials]) {
  val spreadsheetID = "1tk2S_A4LZfeZjoMskbfFXO42_b75A7UkSdhKaQZlDmA"
  val valueRange = {
    val notation = RangeNotation(Range(ColRowPosition("A", 1), ColRowPosition("B", 2)))
    ValueRange(notation, Rows, List(List("1", "2"), List("3", "4")))
  }

  def updateAndGet: F[Either[GsheetsError, List[String]]] = for {
    spreadsheetsValues <- Sync[F].pure(GSheets4s[F](credentials).spreadsheetsValues)
    prog <- new Program[F](spreadsheetsValues)
      .updateAndGet(spreadsheetID, valueRange, UserEntered)
    res = prog.map(_._2.values.flatten)
  } yield res
}

object GSheetsService {
  def apply[F[_]: Sync](credentials: Credentials): F[GSheetsService[F]] =
    Ref.of[F, Credentials](credentials)
      .map(ref => new GSheetsService[F](ref))
}
```

Finally, here's how you could run it using cats-effect's `IO`:

```scala
import cats.effect.IO

object Main {
  def main(args: Array[String]): Unit = {
    val creds = Credentials(accessToken, refreshToken, clientId, clientSecret)
    val io = for {
      service <- GSheetsService[IO](creds)
      res <- service.updateAndGet
    } yield res
    io.unsafeRunSync()
  }
}
```

## Features

Here's the list of currently supported endpoints:

- [spreadsheets](https://developers.google.com/sheets/api/reference/rest/#service-sheetsgoogleapiscom)
  - [ ] `batchUpdate`
  - [ ] `create`
  - [ ] `get`
  - [ ] `getByDataFilter`

- [spreadsheets.developerMetadata](https://developers.google.com/sheets/api/reference/rest/#rest-resource-v4spreadsheetsdevelopermetadata)
  - [ ] `get`
  - [ ] `search`

- [spreadsheets.sheets](https://developers.google.com/sheets/api/reference/rest/#rest-resource-v4spreadsheetssheets)
  - [ ] `copyTo`

- [spreadsheets.values](https://developers.google.com/sheets/api/reference/rest/#rest-resource-v4spreadsheetsvalues)
  - [ ] `append`
  - [ ] `batchClear`
  - [ ] `batchClearByDataFilter`
  - [ ] `batchGet`
  - [ ] `batchGetByDataFilter`
  - [ ] `batchUpdate`
  - [ ] `batchUpdateByDataFilter`
  - [ ] `clear`
  - [x] `get`
  - [x] `update`

### Abstracting over the effect type

gsheets4s abstracts over the effect type `F` by using tagless final encoding for easy composition
between the different APIs and to leave the choice of effect type to the user as long as you provide
an instance of `cats.effect.Sync` for your effect type `F`.

### Typesafe data

gsheets4s uses [refined](https://github.com/fthomas/refined) and
[atto](https://github.com/tpolecat/atto) to make sure
[A1 notation](https://developers.google.com/sheets/api/guides/concepts#a1_notation) is respected at
compile-time when you refer to a group of cells in a spreadsheet.

### Automatic access token refreshing

By providing a refresh token, we make sure that your access token is always valid, refreshing it if
needed.

## Credit

Here are the projects I got inspiration from when building gsheets4s:

- [foorgol](https://github.com/cchantep/foorgol)
- [github4s](https://github.com/47deg/github4s)

Here are the projects used in the library:

- [hammock](https://github.com/pepegar/hammock)
- [cats-effect](https://github.com/typelevel/cats-effect)
- [circe](https://github.com/circe/circe)
- [refined](https://github.com/fthomas/refined)
- [atto](https://github.com/tpolecat/atto)
