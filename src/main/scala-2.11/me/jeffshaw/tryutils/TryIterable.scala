package me.jeffshaw.tryutils

import scala.annotation.varargs
import scala.collection.JavaConverters._

/**
 * This is mostly for use from Java.
 */
object TryIterable {

  @throws[TryForeachException[_]]
  def tryForeach[A](f: java.util.function.Consumer[A], values: java.lang.Iterable[A]): Unit =
    values.asScala.tryForeach(f.accept)

  @varargs
  @throws[TryForeachException[_]]
  def tryForeach[A](f: java.util.function.Consumer[A], values: A*): Unit =
    values.tryForeach(f.accept)

  @varargs
  @throws[TryForeachException[_]]
  def tryForeachInt(f: java.util.function.IntConsumer, values: Int*): Unit =
    values.tryForeach(f.accept)

  @varargs
  @throws[TryForeachException[_]]
  def tryForeachLong(f: java.util.function.LongConsumer, values: Long*): Unit =
    values.tryForeach(f.accept)

  @varargs
  @throws[TryForeachException[_]]
  def tryForeachDouble(f: java.util.function.DoubleConsumer, values: Double*): Unit =
    values.tryForeach(f.accept)

  @varargs
  @throws[TryForeachException[_]]
  def close[A <: AutoCloseable](closeables: A*): Unit =
    closeables.close()

  @throws[TryForeachException[_]]
  def close[A <: AutoCloseable](closeables: java.lang.Iterable[A]): Unit =
    closeables.asScala.close()

  @varargs
  @throws[TryForeachException[_]]
  def tryClose[A <: AutoCloseable](closeables: A*): Unit =
    closeables.tryClose()

  @throws[TryForeachException[_]]
  def tryClose[A <: AutoCloseable](closeables: java.lang.Iterable[A]): Unit =
    closeables.asScala.tryClose()

}
