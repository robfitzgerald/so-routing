package cse.fitzgero.sorouting

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Unit Test Template
  * @note see http://mkuthan.github.io/blog/2015/03/01/spark-unit-testing/
  */
abstract class SparkUnitTestTemplate(testName: String, cores: String = "*") extends SORoutingUnitTestTemplate {

  private val master = s"local[$cores]"
  private val appName = testName
  protected var sc: SparkContext = _

  before {
    val conf = new SparkConf()
      .setMaster(master)
      .setAppName(appName)

    sc = new SparkContext(conf)
  }

  after {
    if (sc != null) {
      sc.stop()
    }
  }
}
