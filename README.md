# Tryutils

This project aims to ease operations on collections where failure should be allowed.

The main problem I wanted solved was to call close on all items of a collection, aggregating exceptions and adding them to the first one as suppressed exceptions.

# Usage

## Dependency

```sbt
libraryDependencies += "me.jeffshaw.tryutils" %% "tryutils" % "1.0.0"
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
  case e: TryForeachException =>
    // recover using `e.failures`
}
```

```java
final Iterable<AutoCloseable> closeables = null;
try {
  TryIterable.tryClose(closeables);
} catch (TryForeachException e) {
  // recover using `e.getFailures()`
}
```
