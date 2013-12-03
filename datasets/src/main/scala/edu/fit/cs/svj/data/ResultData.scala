package edu.fit.cs.svj.data

import scala.collection.mutable.Map

case class ResultData(
    cascade: String, // cascade filename
    averageDetection: Double, // average detection rate 
    time: Long, // in ms
    properties: Map[String, String], // scale -> 1f, etc..
    result: Map[Int, (Int, Int)]) // key -> detected/total