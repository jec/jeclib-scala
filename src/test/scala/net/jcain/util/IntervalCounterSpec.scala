package net.jcain.util

import java.time.Instant
import org.scalatest.{FunSpec, Matchers}

class IntervalCounterSpec extends FunSpec with Matchers {

  describe("MinuteIntervalCounter") {
    describe("constructor") {
      it("initializes count to 0") {
        val counter = new MinuteIntervalCounter
        counter.total shouldBe 0
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
        n shouldBe 120L +- 60L

        val counter = new MinuteIntervalCounter
        counter.increment(5) shouldBe 5
        counter.increment(7, Instant.now.plusSeconds(50)) shouldBe 12
        counter.increment(9, Instant.now.plusSeconds(60)) shouldBe 21
        counter.increment(11, Instant.now.plusSeconds(120)) shouldBe 20
        counter.total shouldBe 20
      }
    }
  }

  describe("HourIntervalCounter") {
    describe("constructor") {
      it("initializes count to 0") {
        val counter = new HourIntervalCounter
        counter.total shouldBe 0
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
        n shouldBe 3600L +- 300L

        val counter = new HourIntervalCounter
        counter.increment(5) shouldBe 5
        counter.increment(7, Instant.now.plusSeconds(3590)) shouldBe 12
        counter.increment(9, Instant.now.plusSeconds(3600)) shouldBe 21
        counter.increment(11, Instant.now.plusSeconds(3910)) shouldBe 27
        counter.total shouldBe 27
      }
    }
  }

  describe("DayIntervalCounter") {
    describe("constructor") {
      it("initializes count to 0") {
        val counter = new DayIntervalCounter
        counter.total shouldBe 0
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
        n shouldBe 3600L +- 300L

        val counter = new DayIntervalCounter
        counter.increment(5) shouldBe 5
        counter.increment(7, Instant.now.plusSeconds(86100)) shouldBe 12
        counter.increment(9, Instant.now.plusSeconds(86400)) shouldBe 21
        counter.increment(11, Instant.now.plusSeconds(90000)) shouldBe 27
        counter.total shouldBe 27
      }
    }
  }

}
