package me.jeffshaw

import scala.collection.mutable.Builder
import scala.util.control.NonFatal

package object tryutils {

  private def suppress(throwables: Iterable[Throwable]): Throwable = {
    val head = throwables.head
    for (tail <- throwables.tail) {
      head.addSuppressed(tail)
    }
    head
  }

  implicit class Extensions[
    A,
    CC[X] <: Iterable[X]
  ](val values: CC[A]
  ) extends AnyVal {
    /**
     * Run `f` on all the values. Any exceptions will be collected.
     */
    def tryFlatMap[B](f: A => IterableOnce[B]): (Seq[(A, Throwable)], CC[B]) = {
      val failures = Seq.newBuilder[(A, Throwable)]
      val results = values.iterableFactory.newBuilder[B]
      for (value <- values) {
        try {
          results ++= f(value)
        } catch {
          case NonFatal(exception) =>
            failures += ((value, exception))
        }
      }
      (failures.result(), results.result().asInstanceOf[CC[B]])
    }

    /**
     * Run `f` on all the values. Any exceptions will be collected.
     */
    def tryMap[B](f: A => B): (Seq[(A, Throwable)], CC[B]) = {
      val failures = Seq.newBuilder[(A, Throwable)]
      val results = values.iterableFactory.newBuilder[B]
      for (value <- values) {
        try {
          results += f(value)
        } catch {
          case NonFatal(exception) =>
            failures += ((value, exception))
        }
      }
      (failures.result(), results.result().asInstanceOf[CC[B]])
    }

    /**
     * Run `f` on all the values, but if any of them throw an exception, throw
     * an aggregate exception.
     */
    @throws[TryForeachException[_]]
    def tryForeach(f: A => Unit): Unit = {
      var failures: Builder[(A, Throwable), Seq[(A, Throwable)]] = null

      for (value <- values) {
        try {
          f(value)
        } catch {
          case NonFatal(e) =>
            if (failures == null) {
              failures = Seq.newBuilder[(A, Throwable)]
            }
            failures += ((value, e))
        }
      }

      if (failures != null) {
        val failuresResult = failures.result()
        throw new TryForeachException(failuresResult)
      }
    }

    /**
     * Close all the values, but stop on the first exception.
     */
    @throws[TryForeachException[_]]
    def close[B >: A]()(implicit ev: B <:< AutoCloseable): Unit = {
      for (value <- values) {
        value.close()
      }
    }

    /**
     * Close all the values. Any exceptions will be recorded and thrown
     * in an aggregate exception.
     */
    @throws[TryForeachException[_]]
    def tryClose[B >: A]()(implicit ev: B <:< AutoCloseable): Unit = {
      tryForeach(_.close())
    }
  }

}
