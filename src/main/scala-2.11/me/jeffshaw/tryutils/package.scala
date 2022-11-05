package me.jeffshaw

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.util.control.NonFatal

package object tryutils {

  private[tryutils] def suppress(throwables: Traversable[Throwable]): Throwable = {
    val head = throwables.head
    for (tail <- throwables.tail) {
      head.addSuppressed(tail)
    }
    head
  }

  implicit class Extensions[+A](val values: TraversableOnce[A]) extends AnyVal {
    /**
     * Run `f` on all the values. Any exceptions will be collected.
     */
    def tryFlatMap[B, That](f: A => TraversableOnce[B])(implicit cbf: CanBuildFrom[_, B, That]): (Seq[(A, Throwable)], That) = {
      val failures = Seq.newBuilder[(A, Throwable)]
      val results = cbf()
      for (value <- values) {
        try {
          results ++= f(value)
        } catch {
          case NonFatal(exception) =>
            failures += ((value, exception))
        }
      }
      (failures.result(), results.result())
    }

    /**
     * Run `f` on all the values. Any exceptions will be collected.
     */
    def tryMap[B, That](f: A => B)(implicit cbf: CanBuildFrom[_, B, That]): (Seq[(A, Throwable)], That) = {
      val failures = Seq.newBuilder[(A, Throwable)]
      val results = cbf()
      for (value <- values) {
        try {
          results += f(value)
        } catch {
          case NonFatal(exception) =>
            failures += ((value, exception))
        }
      }
      (failures.result(), results.result())
    }

    /**
     * Run `f` on all the values, but if any of them throw an exception, throw
     * an aggregate exception.
     */
    def tryForeach(f: A => Unit): Unit = {
      var throwables: Builder[Throwable, Seq[Throwable]] = null
      var failures: Builder[A, Seq[A]] = null

      for (value <- values) {
        try {
          f(value)
        } catch {
          case NonFatal(e) =>
            if (throwables == null) {
              throwables = Seq.newBuilder[Throwable]
              failures = Seq.newBuilder[A]
            }
            throwables += e
            failures += value
        }
      }

      if (throwables != null) {
        val failuresResult = failures.result()
        val suppressed = suppress(throwables.result())
        throw new TryForeachException(failuresResult, suppressed)
      }
    }

    /**
     * Close all the values, but stop on the first exception.
     */
    def close[B >: A]()(implicit ev: B <:< AutoCloseable): Unit = {
      for (value <- values) {
        value.close()
      }
    }

    /**
     * Close all the values. Any exceptions will be recorded and thrown
     * in an aggregate exception.
     */
    def tryClose[B >: A]()(implicit ev: B <:< AutoCloseable): Unit = {
      tryForeach(_.close())
    }
  }

}
