package com.softwaremill.sttp.impl.scalaz

import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import scalaz.~>

import scala.language.higherKinds

object implicits {
  implicit class MappableSttpBackend[R[_], S](val sttpBackend: SttpBackend[R, S]) extends AnyVal {
    def mapK[G[_]: MonadError](f: R ~> G): SttpBackend[G, S] = new MappedKSttpBackend(sttpBackend, f, implicitly)
  }
}

private[scalaz] final class MappedKSttpBackend[F[_], -S, G[_]](wrapped: SttpBackend[F, S],
                                                               mapping: F ~> G,
                                                               val responseMonad: MonadError[G])
    extends SttpBackend[G, S] {
  def send[T](request: Request[T, S]): G[Response[T]] = mapping(wrapped.send(request))

  def close(): Unit = wrapped.close()
}
