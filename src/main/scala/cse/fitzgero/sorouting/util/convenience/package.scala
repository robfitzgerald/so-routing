package cse.fitzgero.sorouting.util

import java.time.LocalTime

package object convenience {

  class PostfixIntTypeNames(x: Int) {
    def iterations: Int = x
    def persons: Int = x
  }

  class PostfixLongTypeNames(x: Long) {
    def seconds: Long = x * 1000L
    def ms: Long = x
    def minutesDeviation: Long = x * 60
    def secondsDeviation: Long = x
  }

  class PostfixDoubleTypeNames(x: Double) {
    def mph: Double = x
    def vph: Double = x
  }

  class PostfixLocalTimeTypeNames(x: LocalTime) {
    def endTime: LocalTime = x
  }

  implicit def decorateIntWithConvenienceFunctions(x: Int): PostfixIntTypeNames = new PostfixIntTypeNames(x)
  implicit def decorateLongWithConvenienceFunctions(x: Long): PostfixLongTypeNames = new PostfixLongTypeNames(x)
  implicit def decorateDoubleWithConvenienceFunctions(x: Double): PostfixDoubleTypeNames = new PostfixDoubleTypeNames(x)
  implicit def decorateLocalTimeWithConvenienceFunctions(x: LocalTime): PostfixLocalTimeTypeNames = new PostfixLocalTimeTypeNames(x)

}
