package edu.fit.cs.svj.model

case class Feature(rectangles: Seq[Rectangle], tilted: Boolean, threshold: Double, leftVal: Option[Double], leftNode: Option[Int], rightVal: Option[Double], rightNode: Option[Int]) {
  
  def valueOf(ii: IntegralImage, x: Int, y: Int, scale: Float, size: (Int, Int)) = {
    val base = Rectangle(x, y, (scale * size._1).toInt, (scale * size._2).toInt)
    val iArea = base.invArea
    val mean = ii.sumOf(base) * iArea
    val varianceNormalizeFactor = (ii.squaredSumOf(base) * iArea - mean * mean) match {
      case i if i > 1 => Math.sqrt(i)
      case _ => 1
    }
    
    val sum = rectangles.foldLeft[Int](0) { (sum, rect) =>
      sum + (ii.sumOf(rect, (x, y), scale) * rect.weight).toInt
    }
    
    (sum * iArea) < (threshold * varianceNormalizeFactor)
  }
}