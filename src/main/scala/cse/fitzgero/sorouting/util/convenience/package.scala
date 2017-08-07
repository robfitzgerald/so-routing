package cse.fitzgero.sorouting.util

package object convenience {
  class PostfixNumberTypeNames(x: Double) {
    def mph: Double = x
    def vph: Double = x
  }

  implicit def decorateDoubleWithConvenienceFunctions(x: Double): PostfixNumberTypeNames = new PostfixNumberTypeNames(x)
  
}
