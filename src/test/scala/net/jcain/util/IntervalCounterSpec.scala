package net.jcain.util

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

import java.time.Instant

class IntervalCounterSpec extends AnyFunSpec with Matchers {
  describe("MinuteIntervalCounter") {
    describe("constructor") {
      it("initializes count to 0") {
        val counter = new MinuteIntervalCounter
        counter.total mustBe 0
      }
    }

    describe("increment()") {
      it("adds to the counter's total and expires counts older than 2 minutes") {
        val counter0 = new MinuteIntervalCounter
        var n = counter0.increment(1)
        var lastN = n
        while (lastN <= n) {
          lastN = n
          n = counter0.increment(1, Instant.now.plusSeconds(n))
        }
        n mustBe 120L +- 60L

        val counter = new MinuteIntervalCounter
        counter.increment(5) mustBe 5
        counter.increment(7, Instant.now.plusSeconds(50)) mustBe 12
        counter.increment(9, Instant.now.plusSeconds(60)) mustBe 21
        counter.increment(11, Instant.now.plusSeconds(120)) mustBe 20
        counter.total mustBe 20
      }
    }
  }

  describe("HourIntervalCounter") {
    describe("constructor") {
      it("initializes count to 0") {
        val counter = new HourIntervalCounter
        counter.total mustBe 0
      }
    }

    describe("increment()") {
      it("adds to the counter's total and expires counts older than 65 minutes") {
        val counter0 = new HourIntervalCounter
        var n = counter0.increment(1)
        var lastN = n
        while (lastN <= n) {
          lastN = n
          n = counter0.increment(1, Instant.now.plusSeconds(n))
        }
        n mustBe 3600L +- 300L

        val counter = new HourIntervalCounter
        counter.increment(5) mustBe 5
        counter.increment(7, Instant.now.plusSeconds(3590)) mustBe 12
        counter.increment(9, Instant.now.plusSeconds(3600)) mustBe 21
        counter.increment(11, Instant.now.plusSeconds(3910)) mustBe 27
        counter.total mustBe 27
      }
    }
  }

  describe("DayIntervalCounter") {
    describe("constructor") {
      it("initializes count to 0") {
        val counter = new DayIntervalCounter
        counter.total mustBe 0
      }
    }

    describe("increment()") {
      it("adds to the counter's total and expires counts older than 25 hours") {
        val counter0 = new HourIntervalCounter
        var n = counter0.increment(1)
        var lastN = n
        while (lastN <= n) {
          lastN = n
          n = counter0.increment(1, Instant.now.plusSeconds(n))
        }
        n mustBe 3600L +- 300L

        val counter = new DayIntervalCounter
        counter.increment(5) mustBe 5
        counter.increment(7, Instant.now.plusSeconds(86100)) mustBe 12
        counter.increment(9, Instant.now.plusSeconds(86400)) mustBe 21
        counter.increment(11, Instant.now.plusSeconds(90000)) mustBe 27
        counter.total mustBe 27
      }
    }
  }
}
