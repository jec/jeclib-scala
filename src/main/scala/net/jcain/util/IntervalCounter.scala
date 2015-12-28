package net.jcain.util

import java.time.Instant
import scala.collection.mutable

/**
  * The `resolution` parameter determines the number of time units of resolution, such
  * that increments occurring within `resolution` time units are counted and
  * aged out together.
  * @param resolution Int number of time units grouped together
  */
abstract class IntervalCounter(val resolution: Int = 5) {

  class Entry(val ts: Long, var count: Int = 1)

  if (resolution <= 0 || factor % resolution != 0)
    throw new IllegalArgumentException(s"resolution must be a positive factor of $factor")

  private var _total: Long = 0
  val entries = mutable.Queue.empty[Entry]

  def total = {
    expire()
    _total
  }

  /**
    * The number of seconds in the time unit by which we will count
    *
    * @return Int
    */
  protected def factor: Int

  /**
    * Returns the maximum number of time units in the interval over which we
    * will count
    *
    * Counts older than this number of time units will be discarded. This
    * defaults to 60 since there are both 60 seconds in a minute and 60 minutes
    * in an hour, which are the most common (time unit, limit) pairs.
    *
    * @return Int
    */
  protected def limit = 60

  /**
    * Increments the count by `count`
    *
    * @param count Int the amount to add to the total
    * @param now java.time.Instant the time to apply the count; for testing only
    * @return Int the new total
    */
  def increment(count: Int = 1, now: Instant = Instant.now): Long = {
    val ts = now.getEpochSecond / factor / resolution

    // update last entry or add a new one
    if (entries.nonEmpty && entries.last.ts == ts)
      entries.last.count += count
    else
      entries += new Entry(ts, count)
    _total += count

    // remove any expired counts
    do_expire(ts)
  }

  /**
    * Expires any counts older than the time limit units.
    *
    * @param now java.time.Instant the time to consider as now; for testing only
    * @return Int the new total
    */
  def expire(now: Instant = Instant.now): Long = {
    do_expire(now.getEpochSecond / factor / resolution)
  }

  protected def do_expire(ts: Long): Long = {
    while (entries.nonEmpty && entries.head.ts < (ts - (limit / resolution))) {
      _total -= entries.head.count
      entries.dequeue
    }
    _total
  }

}
