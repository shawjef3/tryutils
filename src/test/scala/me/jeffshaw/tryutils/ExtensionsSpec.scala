package me.jeffshaw.tryutils

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.TableDrivenPropertyChecks

class ReportsClosed extends AutoCloseable {
  var isClosed = false

  override def close(): Unit = {
    isClosed = true
  }
}

class CloseException extends Exception

class FailsClosed extends AutoCloseable {
  override def close(): Unit = {
    throw new CloseException
  }
}

class ExtensionsSpec extends AnyFunSuite with TableDrivenPropertyChecks {

  test("tryClose all succeed") {
    val count = Table("count", 0, 1, 2)

    forAll(count) { count =>
      val cs = Seq.fill(count)(new ReportsClosed)
      cs.tryClose()
      assert(cs.forall(_.isClosed))
    }
  }

  test("tryClose all fail") {
    val count =
      Table("count", 1, 2)

    forAll(count) { count =>
      val cs = Seq.fill(count)(new FailsClosed)
      val exception =
        intercept[TryForeachException[FailsClosed]] {
          cs.tryClose()
        }

      assertResult(cs)(exception.failures)
      assert(exception.getCause.isInstanceOf[CloseException])
      assertResult(count - 1)(exception.getCause.getSuppressed.length)
    }
  }

  test("tryClose some fail") {
    val cs = Seq[AutoCloseable](new ReportsClosed, new FailsClosed, new ReportsClosed, new FailsClosed)
    val exception =
      intercept[TryForeachException[AutoCloseable]] {
        cs.tryClose()
      }

    assert(
      cs.collect {
        case r: ReportsClosed => r
      } forall (_.isClosed)
    )
    assertResult(cs.filter(_.isInstanceOf[FailsClosed]))(exception.failures)
    assert(exception.getCause.isInstanceOf[CloseException])
    assertResult(1)(exception.getCause.getSuppressed.length)
  }

  test("map") {
    val count = Table("count", 0, 1, 2)

    forAll(count) { count =>
      val cs = Seq.fill(count)(0)
      val (failures, successes) =
        cs.tryMap {
          case n: Int if n % 2 == 0 =>
            n
          case _ =>
            throw new CloseException
        }

      assert(failures.map(_._2).forall(_.isInstanceOf[CloseException]))
      assertResult(count)(failures.size + successes.size)
    }
  }

  test("flatMap") {
    /*
        0

        1
        s s

        2
        s s f

        3
        s s f s s

        4
        s s f s s f

        5
        s s f s s f s s

        count successes failures total
        0     0         0        0
        1     2         0        2
        2     2         1        3
        3     4         1        5
        4     4         2        6
        5     6         2        8
        6     6         3        9
    */

    val counts: Stream[(Int, Int, Int)] =
      for (n <- Stream.from(0)) yield {
        (n, n >> 1, n + (n & 1))
      }

    val countsTable = Table(("count", "expected failures", "expected successes"), counts.take(10): _*)

    forAll(countsTable) { (count, expectedFailureCount, expectedSuccessCount) =>
      val cs = Seq.range(0, count)
      val (failures, successes) =
        cs.tryFlatMap {
          case n if n % 2 == 0 =>
            Seq(n, n)
          case _ =>
            throw new CloseException
        }

      assert(failures.map(_._2).forall(_.isInstanceOf[CloseException]))

      assertResult(expectedFailureCount)(failures.size)
      assertResult(expectedSuccessCount)(successes.size)
    }
  }
}
