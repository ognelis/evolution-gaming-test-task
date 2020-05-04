package com.evolutiongaming.maqexoju.components.util

import java.util.concurrent.{ExecutorService, Executors}

import cats.effect.{Resource, Sync}

import scala.concurrent.ExecutionContext

object ExecutionContexts {

  /** Resource yielding an `ExecutionContext` backed by an unbounded thread pool. */
  def cachedThreadPool[F[_]: Sync]: Resource[F, ExecutionContext] = {
    val alloc = Sync[F].delay(Executors.newCachedThreadPool)
    val free  = (es: ExecutorService) => Sync[F].delay(es.shutdown())
    Resource.make(alloc)(free).map(ExecutionContext.fromExecutor)
  }

}
