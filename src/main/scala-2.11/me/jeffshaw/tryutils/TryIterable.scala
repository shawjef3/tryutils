package me.jeffshaw.tryutils

import scala.annotation.varargs
import scala.collection.JavaConverters._

/**
 * This is mostly for use from Java.
 */
object TryIterable {

  def tryForeach[A](f: java.util.function.Consumer[A], values: java.lang.Iterable[A]): Unit =
    values.asScala.tryForeach(f.accept)

  @varargs
  def tryForeach[A](f: java.util.function.Consumer[A], values: A*): Unit =
    values.tryForeach(f.accept)

  @varargs
  def tryForeachInt(f: java.util.function.IntConsumer, values: Int*): Unit =
    values.tryForeach(f.accept)

  @varargs
  def tryForeachLong(f: java.util.function.LongConsumer, values: Long*): Unit =
    values.tryForeach(f.accept)

  @varargs
  def tryForeachDouble(f: java.util.function.DoubleConsumer, values: Double*): Unit =
    values.tryForeach(f.accept)

  @varargs
  def close[A <: AutoCloseable](closeables: A*): Unit =
    closeables.close()

  def close[A <: AutoCloseable](closeables: java.lang.Iterable[A]): Unit =
    closeables.asScala.close()

  @varargs
  def tryClose[A <: AutoCloseable](closeables: A*): Unit =
    closeables.toSeq.tryClose()

  def tryClose[A <: AutoCloseable](closeables: java.lang.Iterable[A]): Unit =
    closeables.asScala.tryClose()

}
