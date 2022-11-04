package me.jeffshaw.tryutils

import scala.util.{Failure, Success, Try}
import scala.collection.mutable.Builder
import scala.collection.IterableOps
import scala.util.control.NonFatal

private def suppress(throwables: Iterable[Throwable]): Throwable =
  val head = throwables.head
  for tail <- throwables.tail do
    head.addSuppressed(tail)
  head
end suppress

extension [
  A,
  CC[X] <: Iterable[X],
  C <: IterableOps[A, CC, C]
](values: C)

  /**
   * Run `f` on all the values. Any exceptions will be collected.
   */
  def tryFlatMap[B](f: A => IterableOnce[B]): (Seq[(A, Throwable)], CC[B]) =
    val failures = Seq.newBuilder[(A, Throwable)]
    val results = values.iterableFactory.newBuilder[B]
    for value <- values do
      Try(f(value)) match
      case Success(result) =>
        results ++= result
      case Failure(exception) =>
        failures += ((value, exception))
    (failures.result(), results.result())

  /**
   * Run `f` on all the values. Any exceptions will be collected.
   */
  def tryMap[B](f: A => B): (Seq[(A, Throwable)], CC[B]) =
    val failures = Seq.newBuilder[(A, Throwable)]
    val results = values.iterableFactory.newBuilder[B]
    for value <- values do
      Try(f(value)) match
      case Success(result) =>
        results += result
      case Failure(exception) =>
        failures += ((value, exception))
    (failures.result(), results.result())

  /**
   * Run `f` on all the values, but if any of them throw an exception, throw
   * an aggregate exception.
   */
  def tryForeach(f: A => Unit): Unit =
    var throwables: Builder[Throwable, CC[Throwable]] = null
    val successes = Seq.newBuilder[A]
    var failures: Builder[A, Seq[A]] = null

    for value <- values do
      try {
        f(value)
        successes += value
      } catch {
        case NonFatal(e) =>
          if throwables == null then
            throwables = values.iterableFactory.newBuilder[Throwable]
            failures = Seq.newBuilder[A]
          throwables += e
          failures += value
      }

    if throwables != null then
      val successesResult = successes.result()
      val failuresResult = failures.result()
      val suppressed = suppress(throwables.result())
      throw new TryForeachException(successesResult, failuresResult, suppressed)

  /**
   * Close all the values, but stop on the first exception.
   */
  def close[B >: A]()(implicit ev: B <:< AutoCloseable): Unit =
    for value <- values do
      value.close()

  /**
   * Close all the values. Any exceptions will be recorded and thrown
   * in an aggregate exception.
   */
  def tryClose[B >: A]()(implicit ev: B <:< AutoCloseable): Unit =
    tryForeach(_.close())
