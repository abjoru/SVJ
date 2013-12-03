package edu.fit.cs.svj.common

class Timer {
  var millis = 0L
  
  private var lastStart = 0L
  
  def reset: Unit = millis = 0L
  def start: Unit = lastStart = System.currentTimeMillis
  def stop: Long = {
    val elapsed = System.currentTimeMillis - lastStart
    millis += elapsed
    elapsed
  }

}