package edu.fit.cs.svj.model

case class Rectangle(x: Int, y: Int, width: Int, height: Int, weight: Float = 1f) {
  def area = width * height
  def invArea = 1d / area
  def bottom = y + height
  def right = x + width
  
  def topLeft = (x, y)
  def topLeft(off: (Int, Int), scale: Float) = (off._1 + (scale * x).toInt, off._2 + (scale * y).toInt)
  
  def topRight = (right, y)
  def topRight(off: (Int, Int), scale: Float) = (off._1 + (scale * right).toInt, off._2 + (scale * y).toInt)
  
  def bottomLeft = (x, bottom)
  def bottomLeft(off: (Int, Int), scale: Float) = (off._1 + (scale * x).toInt, off._2 + (scale * bottom).toInt)
  
  def bottomRight = (right, bottom)
  def bottomRight(off: (Int, Int), scale: Float) = (off._1 + (scale * right).toInt, off._2 + (scale * bottom).toInt)
}

object Rectangle {
  
  def apply(xmlString: String, delim: String): Rectangle = {
    val Array(x, y, w, h, wg) = xmlString split delim
    new Rectangle(x.toInt, y.toInt, w.toInt, h.toInt, wg.toFloat)
  }
  
}