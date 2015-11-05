package net.jcain.util

class DayIntervalCounter extends IntervalCounter(1) {

  /**
    * Count by the hour (3600 seconds in an hour)
    *
    * @return Int
    */
  def factor = 3600

  /**
    * Interval is a day, and we're counting by the hour
    *
    * @return Int
    */
  override def limit = 24

}
