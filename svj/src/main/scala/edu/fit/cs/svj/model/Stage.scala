package edu.fit.cs.svj.model

case class Stage(classifiers: Seq[Classifier], threshold: Option[Double], parentIndex: Int, nextIndex: Int) {
  
  def check(ii: IntegralImage, x: Int, y: Int, scale: Float, size: (Int, Int)): Boolean = threshold match {
    case Some(t) => classifiers.foldLeft[Double](0d)((sum, cl) => sum + cl.valueOf(ii, x, y, scale, size)) > t
    case None => false
  }
  
}