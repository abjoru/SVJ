package edu.fit.cs.svj.data

case class ImageData(val id: Int, val path: String, val coord: Option[Coord])

case class Coord(val x: Int, val y: Int) {

  /**
   * Determine if we have a true positive. The local x,y coordinate describes
   * the center of the face within the image. If the given rectangle does not
   * cover this coordinate, we may assume that we have a false positive.
   */
  def within(x: Int, y: Int, width: Int, height: Int) =
    (x to x + width).contains(this.x) && (y to y + height).contains(this.y)
}
