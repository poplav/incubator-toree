package com.ibm.spark.interpreter.broker

import org.apache.commons.exec.{ExecuteException, ExecuteResultHandler}
import org.slf4j.LoggerFactory

/**
 * Represents the handler for events triggered by the broker process.
 *
 * @param brokerBridge The bridge to reset when the process fails or completes
 * @param restartOnFailure If true, restarts the process if it fails
 * @param restartOnCompletion If true, restarts the process if it completes
 */
class BrokerProcessHandler(
  private val brokerBridge: BrokerBridge,
  private val restartOnFailure: Boolean,
  private val restartOnCompletion: Boolean
) extends ExecuteResultHandler with BrokerName {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val capitalizedBrokerName = brokerName.capitalize
  private val resetMessage = s"$capitalizedBrokerName was reset!"

  private var performReset: String => Unit = (_) => {}
  private var performRestart: () => Unit = () => {}

  /**
   * Sets the reset method used when a reset of the process is asked.
   *
   * @param resetMethod The method to use for resetting the process
   */
  def setResetMethod(resetMethod: String => Unit): Unit =
    performReset = resetMethod

  /**
   * Sets the restart method used when a restart of the process is asked.
   *
   * @param restartMethod The method to use for restarting the process
   */
  def setRestartMethod(restartMethod: () => Unit): Unit =
    performRestart = restartMethod

  override def onProcessFailed(ex: ExecuteException): Unit = {
    logger.error(s"$capitalizedBrokerName process failed: $ex")
    performReset(resetMessage)

    if (restartOnFailure) performRestart()
  }

  override def onProcessComplete(exitValue: Int): Unit = {
    logger.error(s"$capitalizedBrokerName process exited: $exitValue")
    performReset(resetMessage)

    if (restartOnCompletion) performRestart()
  }
}
