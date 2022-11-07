package me.jeffshaw.tryutils

import java.util.{List => JavaList}
import scala.collection.JavaConverters._

/**
 * When closing a collection of [[AutoCloseable]]s fails, this exception contains the values that could
 * and could not be closed.
 */
class TryForeachException[A](
  val failures: Seq[(A, Throwable)],
  message: String,
  enableSuppression: Boolean,
  writableStackTrace: Boolean
) extends Exception(
  message,
  if (failures.isEmpty) null else TryForeachException.suppress(failures.map(_._2)),
  enableSuppression,
  writableStackTrace
) {
  def this(failures: Seq[(A, Throwable)], message: String) =
    this(failures, message, true, true)

  def this(failures: Seq[(A, Throwable)]) =
    this(failures, null)

  def getFailures(): JavaList[(A, Throwable)] =
    failures.asJava

  def getCauses(): JavaList[Throwable] = {
    failures.map(_._2).asJava
  }

  def getValues(): JavaList[A] = {
    failures.map(_._1).asJava
  }
}

object TryForeachException {
  private def suppress(throwables: Iterable[Throwable]): Throwable = {
    val head = throwables.head
    for (tail <- throwables.tail) {
      head.addSuppressed(tail)
    }
    head
  }
}
