package edu.fit.cs.svj

package object model {

  /**
   * Type alias describing a point
   */
  type Point = (Int, Int)

  /**
   * Type alias describing an image
   */
  type ImageDesc = Array[Array[Int]]

  def distanceEquality(r1: Rectangle, r2: Rectangle, distance: Int): Boolean = 
    de(r1, r2, distance) || re(r1, r2)

  private def de(r1: Rectangle, r2: Rectangle, distance: Int) =
    r2.x <= r1.x + distance && r2.x >= r1.x - distance &&
    r2.y <= r1.y + distance && r2.y >= r1.y - distance &&
    r2.width <= (r1.width * 1.2).toInt && (r2.width * 1.2).toInt >= r1.width
    
  private def re(r1: Rectangle, r2: Rectangle) = 
    r1.x >= r2.x && 
    r1.y >= r2.y && 
    r1.x + r1.width <= r2.x + r2.width && 
    r1.y + r1.height <= r2.y + r2.height

}