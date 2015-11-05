package net.jcain.util

class HourIntervalCounter extends IntervalCounter {

  /**
    * Count by the minute (60 seconds in a minute)
    *
    * @return Int
    */
  def factor = 60

}
