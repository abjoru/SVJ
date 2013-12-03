package edu.fit.cs.svj

import java.awt.image.BufferedImage

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

import edu.fit.cs.svj.model.{Cascade, IntegralImage, Point, Rectangle, Stage, distanceEquality}

class SVJDetector(baseScale: Float, scaleInc: Float, increment: Float, minNeighbors: Int, cannyPruning: Boolean) {
  
  def detect(image: BufferedImage, cascade: Cascade) = {
    val ii = IntegralImage(image)
    val width = image.getWidth
    val height = image.getHeight
    val maxScale = Math.min(width.toFloat / cascade.windowSize._1, height.toFloat / cascade.windowSize._2)

    @inline
    @tailrec
    def processStage(xy: Point, scale: Float, stage: Seq[Stage]): Boolean = stage match {
      case Nil => true
      //case s :: Nil => s.check(ii, xy._1, xy._2, scale, cascade.windowSize)
      case s :: sx => s.check(ii, xy._1, xy._2, scale, cascade.windowSize) match {
        case false => false
        case true => processStage(xy, scale, sx)
      }
    }

    @inline
    @tailrec
    def loop(scale: Float, rects: List[Rectangle]): List[Rectangle] = scale match {
      case s if s >= maxScale => rects
      case _ =>
        val step = (scale * cascade.windowSize._1 * increment).toInt
        val size = (scale * cascade.windowSize._1).toInt

        val rectangles = ListBuffer[Rectangle](rects: _*)
        val xIter = (0 until (width - size) by step).iterator
        
        while (xIter.hasNext) {
          val x = xIter.next
          val yIter = (0 until (height - size) by step).iterator
          while (yIter.hasNext) {
            val y = yIter.next
            if (processStage((x, y), scale, cascade.stages))
              rectangles += Rectangle(x, y, size, size)
          }
        }

        loop(scale * scaleInc, rectangles.toList)
    }

    merge(loop(baseScale, Nil), minNeighbors)
  }
  
  def merge(rectangles: List[Rectangle], minNeighbors: Int) = rectangles match {
    case Nil => Nil
    case x :: Nil => List(x)
    case x :: xs =>
      internalMerge(rectangles, minNeighbors)
  }
  
  private def internalMerge(rectangles: List[Rectangle], minNeighbors: Int) = {
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
    
    result
  }

}