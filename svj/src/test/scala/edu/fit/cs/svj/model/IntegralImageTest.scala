package edu.fit.cs.svj.model

import org.scalatest.FunSuite
import edu.fit.cs.svj.common._
import javax.imageio.ImageIO
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import javax.imageio.spi.IIORegistry
import org.monte.media.pgm.PGMImageReaderSpi
import java.awt.image.BufferedImage

@RunWith(classOf[JUnitRunner])
class IntegralImageTest extends FunSuite {
  IIORegistry.getDefaultInstance().registerServiceProvider(new PGMImageReaderSpi)
  
  val pgmImage = ImageIO.read(getClass.getResource("/orl_faces/s9/1.pgm"))
  val jpgImage = ImageIO.read(getClass.getResource("/lena.jpg"))
  
  test("Color model") {
    assert(pgmImage.getType === BufferedImage.TYPE_BYTE_GRAY)
    assert(jpgImage.getType === BufferedImage.TYPE_3BYTE_BGR)
  }

}