package me.jeffshaw.tryutils

import scala.collection.JavaConverters._

/**
 * When closing a collection of [[AutoCloseable]]s fails, this exception contains the values that could
 * and could not be closed.
 */
class TryForeachException[A](
  val successes: Seq[A],
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
  def this(successes: Seq[A], failures: Seq[A], message: String, cause: Throwable) =
    this(successes, failures, message, cause, true, true)

  def this(successes: Seq[A], failures: Seq[A], message: String) =
    this(successes, failures, message, null)

  def this(successes: Seq[A], failures: Seq[A], cause: Throwable) =
    this(successes, failures, null, cause)

  def this(successes: Seq[A], failures: Seq[A]) =
    this(successes, failures, null: String)

  def getSuccesses(): java.util.List[A] =
    successes.asJava

  def getFailures(): java.util.List[A] =
    failures.asJava
}
