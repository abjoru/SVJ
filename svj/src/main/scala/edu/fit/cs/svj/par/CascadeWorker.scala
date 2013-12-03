package edu.fit.cs.svj.par

import akka.actor.Actor
import edu.fit.cs.svj.model.IntegralImage
import edu.fit.cs.svj.model.Rectangle
import edu.fit.cs.svj.model.Cascade
import edu.fit.cs.svj.model.Point
import scala.collection.mutable.ListBuffer
import edu.fit.cs.svj.model.Stage
import scala.annotation.tailrec

class CascadeWorker extends Actor {
  private var cascade: Cascade = _
  private var baseScale: Float = _
  private var scaleInc: Float = _
  private var increment: Float = _
  private var minNeighbors: Int = _

  def receive = {
    case Init(cascade, baseScale, scaleInc, increment, minNeighbors) =>
      CascadeWorker.this.cascade = cascade
      CascadeWorker.this.baseScale = baseScale
      CascadeWorker.this.scaleInc = scaleInc
      CascadeWorker.this.increment = increment
      CascadeWorker.this.minNeighbors = minNeighbors
    case Merge(id, rectangles, replyTo) =>
      replyTo ! Result(id, merge(rectangles))
    case Work(id, scale, rectangles, integralImage, replyTo) =>
      val max = maxScale(integralImage)
      
      scale match {
        case s if s >= max => sender ! Result(id, rectangles)
        case _ =>
          val step = (scale * cascade.windowSize._1 * increment).toInt
          val size = (scale * cascade.windowSize._2).toInt
          val rects = ListBuffer[Rectangle](rectangles: _*)
          val xIterator = (0 until (integralImage.width - size) by step).iterator

          @inline
          @tailrec
          def passesStage(xy: Point, scale: Float, stages: Seq[Stage], integralImage: IntegralImage): Boolean = stages match {
            case Nil => true
            case s :: sx => s.check(integralImage, xy._1, xy._2, scale, cascade.windowSize) match {
              case false => false
              case true => passesStage(xy, scale, sx, integralImage)
            }
          }

          while (xIterator.hasNext) {
            val x = xIterator.next
            val yIterator = (0 until (integralImage.height - size) by step).iterator
            while (yIterator.hasNext) {
              val y = yIterator.next
              if (passesStage((x, y), scale, cascade.stages, integralImage))
                rects += Rectangle(x, y, size, size)
            }
          }

          val newScale = scale * scaleInc
          if (newScale >= max) sender ! Merge(id, rectangles, replyTo)
          else sender ! Work(id, newScale, rects.toList, integralImage, replyTo)
      }
  }
  
  private def merge(rectangles: List[Rectangle]): List[Rectangle] = rectangles match {
    case Nil => Nil
    case x :: Nil => List(x)
    case x :: xs => internalMerge(rectangles)
  }
  
  private def internalMerge(rectangles: List[Rectangle]): List[Rectangle] = {
    import edu.fit.cs.svj.model._
    
    val result = new ListBuffer[Rectangle]
    val temp = Array.ofDim[Int](rectangles.size)
    var nbClasses = 0;
    var i = 0
    
    while (i < rectangles.size) {
      var found = false
      var j = 0
      while (j < i) {
        if (distanceEquality(rectangles(j), rectangles(i), (rectangles(j).width * .2).toInt)) {
          found = true
          temp(i) = temp(j)
        }
        
        j += 1
      }
      
      if (!found) {
        temp(i) = nbClasses
        nbClasses += 1
      }
      
      i += 1
    }
    
    val neighbors = Array.fill(nbClasses)(0)
    val rect = Array.fill(nbClasses)(Rectangle(0, 0, 0, 0))
    
    i = 0
    while (i < rectangles.size) {
      neighbors(temp(i)) = neighbors(temp(i)) + 1
      val r = rect(temp(i))
      val x = r.x + rectangles(i).x
      val y = r.y + rectangles(i).y
      val w = r.width + rectangles(i).width
      val h = r.height + rectangles(i).height
      rect(temp(i)) = Rectangle(x, y, w, h)
      i += 1
    }
    
    i = 0
    while (i < nbClasses) {
      val n = neighbors(i)
      if (n >= minNeighbors) {
        val x = (rect(i).x * 2 + n) / (2 * n)
        val y = (rect(i).y * 2 + n) / (2 * n)
        val w = (rect(i).width * 2 + n) / (2 * n)
        val h = (rect(i).height * 2 + n) / (2 * n)
        result += Rectangle(x, y, w, h)
      }
      i += 1
    }
    
    result.toList
  }
  
  private def maxScale(ii: IntegralImage) = 
    Math.max(ii.width.toFloat / cascade.windowSize._1, ii.height.toFloat / cascade.windowSize._2)
}