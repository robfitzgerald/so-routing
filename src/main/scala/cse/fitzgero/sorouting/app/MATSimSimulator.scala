package cse.fitzgero.sorouting.app

import org.apache.log4j.{Level, Logger}
import org.matsim.core.config.Config
import org.matsim.core.controler.{ControlerListenerManagerImpl, Injector}
import org.matsim.core.network.NetworkImpl
import org.matsim.core.mobsim.qsim.MobsimListenerManager
import org.matsim.core.events.EventsManagerImpl
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring
import org.matsim.core.utils.io.MatsimXmlParser
import org.matsim.core.utils.misc.Counter
import org.matsim.run.Controler

/**
  * wanted to design this to help quiet the logging from MATSim, but, I can't seem to "access" some of the classes, such as
  * Injector, NetworkImpl, and MobsimListenerManager. instead, globally squashing log4j.
  */
trait MATSimSimulator {
  def suppressMATSimInfoLogging(): Unit = {

    // this deactivates INFO logging globally :-(
    Logger.getRootLogger.setLevel(Level.WARN)
    Logger.getLogger(classOf[CharyparNagelActivityScoring]).setLevel(Level.ERROR)

    // TODO: figure out how to shut off all MATSim logging without shutting off log4j

//    Logger.getLogger(classOf[Config]).setLevel(Level.WARN)
//    Logger.getLogger(classOf[EventsManagerImpl]).setLevel(Level.WARN)
//    Logger.getLogger(classOf[Controler]).setLevel(Level.WARN)
//    Logger.getLogger(classOf[Counter]).setLevel(Level.WARN)
////    Logger.getLogger(classOf[NetworkImpl]).setLevel(Level.WARN)
//    //    Logger.getLogger(classOf[Injector]).setLevel(Level.WARN)
//    //    Logger.getLogger(classOf[MobsimListenerManager]).setLevel(Level.WARN)
//    Logger.getLogger(classOf[MatsimXmlParser]).setLevel(Level.WARN)
  }
}
