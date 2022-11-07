package me.jeffshaw.tryutils

import scala.util.{Failure, Success, Try}
import scala.collection.mutable.Builder
import scala.collection.IterableOps
import scala.util.control.NonFatal

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
  end tryFlatMap

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
  end tryMap

  /**
   * Run `f` on all the values, but if any of them throw an exception, throw
   * an aggregate exception.
   */
  @throws[TryForeachException[_]]
  def tryForeach(f: A => Unit): Unit =
    var failures: Builder[(A, Throwable), Seq[(A, Throwable)]] = null

    for value <- values do
      try {
        f(value)
      } catch {
        case NonFatal(e) =>
          if failures == null then
            failures = Seq.newBuilder
          failures += ((value, e))
      }

    if failures != null then
      val failuresResult = failures.result()
      throw new TryForeachException(failuresResult)
  end tryForeach

  /**
   * Close all the values, but stop on the first exception.
   */
  @throws[TryForeachException[_]]
  def close[B >: A]()(implicit ev: B <:< AutoCloseable): Unit =
    for value <- values do
      value.close()
  end close

  /**
   * Close all the values. Any exceptions will be recorded and thrown
   * in an aggregate exception.
   */
  @throws[TryForeachException[_]]
  def tryClose[B >: A]()(implicit ev: B <:< AutoCloseable): Unit =
    tryForeach(_.close())
  end tryClose

end extension
