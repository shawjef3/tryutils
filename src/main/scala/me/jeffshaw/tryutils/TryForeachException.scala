package me.jeffshaw.tryutils

import scala.collection.JavaConverters._

/**
 * When closing a collection of [[AutoCloseable]]s fails, this exception contains the values that could
 * and could not be closed.
 */
class TryForeachException[A](
  val failures: Seq[A],
  message: String,
  cause: Throwable,
  enableSuppression: Boolean,
  writableStackTrace: Boolean
) extends Exception(
  message,
  cause,
  enableSuppression,
  writableStackTrace
) {
  def this(failures: Seq[A], message: String, cause: Throwable) =
    this(failures, message, cause, true, true)

  def this(failures: Seq[A], message: String) =
    this(failures, message, null)

  def this(failures: Seq[A], cause: Throwable) =
    this(failures, null, cause)

  def this(failures: Seq[A]) =
    this(failures, null: String)

  def getFailures(): java.util.List[A] =
    failures.asJava
}
