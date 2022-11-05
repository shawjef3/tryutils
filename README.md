# Tryutils

This project aims to ease operations on collections where failure should be allowed.

The main problem I wanted solved was to call close on all items of a collection, aggregating exceptions and adding them to the first one as suppressed exceptions.

# Usage

## Dependency

```sbt
libraryDependencies += "me.jeffshaw.tryutils" %% "tryutils" % "1.0.1"
```

## Imports

### Scala

```scala
import me.jeffshaw.tryutils._
```

### Java

```java
import me.jeffshaw.tryutils.TryForeachException;
import me.jeffshaw.tryutils.TryIterable;
```

## tryClose

### Scala

```scala
val closeables: Seq[AutoCloseable] = null
try {
  closeables.tryClose()
} catch {
  case e: TryForeachException[AutoCloseable] =>
    // recover using `e.failures`
}
```

### Java

```java
final Iterable<AutoCloseable> closeables = null;
try {
  TryIterable.tryClose(closeables);
} catch (TryForeachException e) {
  // recover using `e.getFailures()`
}
```

## tryForeach

The underlying mechanism for `tryClose` is `tryForeach`. It is the same, exception instead of calling `AutoCloseable#close()`,
it will call a function of your choosing.

### Scala

```scala
val ints: Seq[Int] = null
try {
  ints.tryForeach(i => if (i == 0) throw new Exception())
} catch {
  case e: TryForeachException[AutoCloseable] =>
  // recover using `e.failures`
}
```

### Java

```java
final Iterable<Object> closeables = null;
try {
  TryIterable.tryForEach(
    (Object o) -> {
      if (isInvalid(o)) {
        throw new RuntimeException();
      }
    },
    closeables);
} catch (TryForeachException e) {
  // recover using `e.getFailures()`
}
```

There are also specialized methods for primitives.

```java
final int[] ints = null;
try {
  TryIterable.tryForEachInt(
    (int i) -> {
      if (isInvalid(i)) {
        throw new RuntimeException();
      }
    },
    ints);
} catch (TryForeachException e) {
  // recover using `e.getFailures()`
}
```
