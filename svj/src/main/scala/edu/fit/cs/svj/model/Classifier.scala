package edu.fit.cs.svj.model

import scala.annotation.tailrec

case class Classifier(features: Seq[Feature]) {
  
  def valueOf(ii: IntegralImage, x: Int, y: Int, scale: Float, size: (Int, Int)): Double = {
    @inline
    @tailrec
    def proc(feature: Feature): Double = feature.valueOf(ii, x, y, scale, size) match {
      case true => feature.leftVal match {
        case Some(lv) => lv
        case None => feature.leftNode match {
          case None => 0d // No value
          case Some(ln) => proc(features(ln))
        }
      }
      case false => feature.rightVal match {
        case Some(rv) => rv
        case None => feature.rightNode match {
          case None => 0d // No value
          case Some(rn) => proc(features(rn))
        }
      }
    }
    
    proc(features(0))
  }
}