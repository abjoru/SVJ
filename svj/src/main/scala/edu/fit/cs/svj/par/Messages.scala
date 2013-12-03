package edu.fit.cs.svj.par

import edu.fit.cs.svj.model.Rectangle
import edu.fit.cs.svj.model.IntegralImage
import edu.fit.cs.svj.model.Cascade
import java.awt.image.BufferedImage
import akka.actor.ActorRef

sealed trait SVJMessage
case class Init(cascade: Cascade, baseScale: Float, scaleInc: Float, increment: Float, minNeighbors: Int) extends SVJMessage
case class Work(id: String, scale: Float, rectangles: List[Rectangle], integralImage: IntegralImage, replyTo: ActorRef) extends SVJMessage
case class Merge(id: String, rectangles: List[Rectangle], replyTo: ActorRef) extends SVJMessage
case class Result(id: String, rectangles: List[Rectangle]) extends SVJMessage

case class Detect(id: String, image: IntegralImage) extends SVJMessage

case object MasterRegistration