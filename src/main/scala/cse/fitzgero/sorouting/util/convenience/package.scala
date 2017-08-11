package cse.fitzgero.sorouting.util

package object convenience {

  class PostfixIntTypeNames(x: Int) {
    def iterations: Int = x
    def persons: Int = x
  }

  class PostfixLongTypeNames(x: Long) {
    def seconds: Long = x * 1000L
  }

  class PostfixDoubleTypeNames(x: Double) {
    def mph: Double = x
    def vph: Double = x
  }

  implicit def decorateIntWithConvenienceFunctions(x: Int): PostfixIntTypeNames = new PostfixIntTypeNames(x)
  implicit def decorateLongWithConvenienceFunctions(x: Long): PostfixLongTypeNames = new PostfixLongTypeNames(x)
  implicit def decorateDoubleWithConvenienceFunctions(x: Double): PostfixDoubleTypeNames = new PostfixDoubleTypeNames(x)

}
