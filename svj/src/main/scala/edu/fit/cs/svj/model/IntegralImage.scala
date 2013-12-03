package edu.fit.cs.svj.model

import java.awt.image.BufferedImage
import scala.collection.mutable.ArrayBuffer

/**
 * All BufferedImage's have an upper left corner coordinate of (0, 0).
 */
case class IntegralImage(width: Int, height: Int, integralImage: ImageDesc, scaledIntegralImage: ImageDesc) {

  /**
   * Calculate the sum of the <code>rect</code> in this integral image.
   * TODO need more scientific descriptions.
   */
  def sumOf(rect: Rectangle) = {
    def sel(p: Point) = integralImage(p._1)(p._2)

    sel(rect.bottomRight) - sel(rect.bottomLeft) - sel(rect.topRight) + sel(rect.topLeft)
  }

  def sumOf(r: Rectangle, off: Point, scale: Float) = {
    def sel(p: Point) = integralImage(p._1)(p._2)

    sel(r.bottomRight(off, scale)) - sel(r.bottomLeft(off, scale)) - sel(r.topRight(off, scale)) + sel(r.topLeft(off, scale))
  }

  /**
   * Calculates the squared sum of the <code>rect</code> in this integral image.
   * TODO need more scientific descriptions.
   */
  def squaredSumOf(rect: Rectangle) = {
    def sel(p: Point) = scaledIntegralImage(p._1)(p._2)

    sel(rect.bottomRight) - sel(rect.bottomLeft) - sel(rect.topRight) + sel(rect.topLeft)
  }

}

/**
 * IntegralImage companion object.
 */
object IntegralImage {
  val R_MASK = 0x00ff0000
  val G_MASK = 0x0000ff00
  val B_MASK = 0x000000ff

  def apply(image: BufferedImage) = {
    val _integralImage = Array.ofDim[Int](image.getWidth, image.getHeight)
    val _integralImageScaled = Array.ofDim[Int](image.getWidth, image.getHeight)
    
    var y = 0
    while (y < image.getHeight) {
      var x = 0
      while (x < image.getWidth) {
        val gray = grayScale(image, x, y)
        calculate(x, y, gray, _integralImage)
        calculate(x, y, gray * gray, _integralImageScaled)
        
        x += 1
      }
      
      y += 1
    }

    /*
    for (y <- (0 until image.getHeight); x <- (0 until image.getWidth)) {
      val gray = grayScale(image, x, y)
      calculate(x, y, gray, _integralImage)
      calculate(x, y, gray * gray, _integralImageScaled)
    }*/

    new IntegralImage(image.getWidth, image.getHeight, _integralImage, _integralImageScaled)
  }
  
  private def calculate(x: Int, y: Int, value: Int, target: ImageDesc) {
    val u = if (x > 0) target(x - 1)(y) else 0
    val r = if (y > 0) target(x)(y - 1) else 0
    val ur = if (x > 0 && y > 0) target(x - 1)(y - 1) else 0
    target(x)(y) = value + u + r - ur
  }

  private def grayScale(img: BufferedImage, x: Int, y: Int) = {
    val rgb = img.getRGB(x, y)
    val (r, g, b) = ((rgb & R_MASK) >> 16, (rgb & G_MASK) >> 8, (rgb & B_MASK))
    (r * 30 + g * 59 + b * 11) / 100
  }

}