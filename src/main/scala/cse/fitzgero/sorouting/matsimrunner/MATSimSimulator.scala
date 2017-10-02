package cse.fitzgero.sorouting.matsimrunner

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.{Level, Logger}
import org.matsim.analysis.{CalcLinkStats, ModeStatsControlerListener, ScoreStatsControlerListener, TravelDistanceStats}
import org.matsim.api.core.v01.Scenario
import org.matsim.api.core.v01.network.Network
import org.matsim.api.core.v01.population.Population
import org.matsim.core.config.consistency.VspConfigConsistencyCheckerImpl
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup
import org.matsim.core.config.{ConfigReader, Config => MATSimConfig}
import org.matsim.core.controler._
import org.matsim.core.events.EventsManagerImpl
import org.matsim.core.gbl.Gbl
import org.matsim.core.mobsim.qsim.QSim
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine
import org.matsim.core.network.io.{MatsimNetworkReader, NetworkWriter}
import org.matsim.core.population.PopulationUtils
import org.matsim.core.population.io.{PopulationReader, PopulationWriter}
import org.matsim.core.replanning.StrategyManager
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring
import org.matsim.core.utils.io.MatsimXmlParser
import org.matsim.core.utils.misc.Counter
import org.matsim.lanes.data.LanesWriter
import org.matsim.vehicles.VehicleWriterV1

trait MATSimSimulator {

  suppressMATSimInfoLogging()
  setRootLogging()

  /**
    * if the config logging is set, apply it to the root logging status
    */
  def setRootLogging(): Unit = {
    val config: Config = ConfigFactory.load()
    val logLevelOption: Option[String] = if (config.hasPath("soRouting.application.logging")) Some(config.getString("soRouting.application.logging")) else None
    logLevelOption match {
      case Some(level) => Logger.getRootLogger.setLevel(Level.toLevel(level))
      case None => // do nothing
    }
  }

  /**
    * tries DESPERATELY to quiet down MATSim logging
    * @param level optionally set the log level for all MATSim classes I could find
    */
  def suppressMATSimInfoLogging(level: Level = Level.ERROR): Unit = {

    Logger.getLogger(classOf[CharyparNagelActivityScoring]).setLevel(level)
    Logger.getLogger(classOf[MATSimConfig]).setLevel(level)
    Logger.getLogger(classOf[EventsManagerImpl]).setLevel(level)
    Logger.getLogger(classOf[Controler]).setLevel(level)
    Logger.getLogger(classOf[Counter]).setLevel(level)
    Logger.getLogger(classOf[Injector]).setLevel(level)
    Logger.getLogger(classOf[MatsimXmlParser]).setLevel(level)
    Logger.getLogger(classOf[Network]).setLevel(level)
    Logger.getLogger(classOf[Scenario]).setLevel(level)
    Logger.getLogger(classOf[Population]).setLevel(level)
    Logger.getLogger(classOf[NetworkWriter]).setLevel(level)
    Logger.getLogger(classOf[ConfigReader]).setLevel(level)
    Logger.getLogger(classOf[ControlerListenerManagerImpl]).setLevel(level)
    Logger.getLogger(classOf[Gbl]).setLevel(level)
    Logger.getLogger(classOf[StrategyManager]).setLevel(level)
    Logger.getLogger(classOf[AbstractController]).setLevel(level)
    Logger.getLogger(classOf[VehicleWriterV1]).setLevel(level)
    Logger.getLogger(classOf[ControlerUtils]).setLevel(level)
    Logger.getLogger(classOf[VspConfigConsistencyCheckerImpl]).setLevel(level)
    Logger.getLogger(classOf[PopulationReader]).setLevel(level)
    Logger.getLogger(classOf[QNetsimEngine]).setLevel(level)
    Logger.getLogger(classOf[LanesWriter]).setLevel(level)
    Logger.getLogger(classOf[PlanCalcScoreConfigGroup]).setLevel(level)
    Logger.getLogger(classOf[PopulationWriter]).setLevel(level)
    Logger.getLogger(classOf[QSim]).setLevel(level)
    Logger.getLogger(classOf[TravelDistanceStats]).setLevel(level)
    Logger.getLogger(classOf[OutputDirectoryLogging]).setLevel(level)
    Logger.getLogger(classOf[CalcLinkStats]).setLevel(level)
    Logger.getLogger(classOf[PopulationUtils]).setLevel(level)
    Logger.getLogger(classOf[MatsimNetworkReader]).setLevel(level)
    Logger.getLogger(classOf[ModeStatsControlerListener]).setLevel(level)
    Logger.getLogger(classOf[AbstractController]).setLevel(level)
    Logger.getLogger(classOf[ScoreStatsControlerListener]).setLevel(level)

    // cannot seem to deactivate these

    //  Logger.getLogger(classOf[MobsimListenerManager]).setLevel(level)
    //  Logger.getLogger(classOf[NetworkImpl]).setLevel(level)
    //  Logger.getLogger(classOf[NewScoreAssignerImpl]).setLevel(level)
    //  Logger.getLogger(classOf[PopulationImpl]).setLevel(level)
    //  Logger.getLogger(classOf[ScenarioLoaderImpl]).setLevel(level)
    //  Logger.getLogger(classOf[MobsimListenerManager]).setLevel(level)
    //  Logger.getLogger(classOf[PlansDumpingImpl]).setLevel(level)
    //  Logger.getLogger(classOf[LegHistogramListener]).setLevel(level)
    //  Logger.getLogger(classOf[LegTimesControlerListener]).setLevel(level)
    //  Logger.getLogger(classOf[NetworkImpl]).setLevel(level)
    //  Logger.getLogger(classOf[MatsimRuntimeModifications]).setLevel(level)
    //  Logger.getLogger(classOf[GenericStrategyManager]).setLevel(level)

  }
}
