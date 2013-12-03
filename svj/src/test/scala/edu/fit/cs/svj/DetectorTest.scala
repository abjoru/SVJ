package edu.fit.cs.svj

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DetectorTest extends FunSuite {
  
  test("calculateCount") {
    val imgWidth = 128
    val imgHeight = 128
    val windowWidth = 24
    val windowHeight = 24
    val boundedWidth = imgWidth - 10
    val boundedHeight = imgHeight - 10
    
    val scale = 1.2
    
    def count(iter: Double, c: Int): Int = iter match {
      case i if (i * windowWidth) < boundedWidth && (i * windowHeight) < boundedHeight => count(i * scale, c + 1)
      case _ => c
    }
    
    println(count(1d, 0))
  }

}