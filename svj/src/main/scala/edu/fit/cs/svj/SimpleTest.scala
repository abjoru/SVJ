package edu.fit.cs.svj

import edu.fit.cs.svj.data.Datasets
import javax.imageio.ImageIO
import edu.fit.cs.svj.model.Cascade
import javax.imageio.spi.IIORegistry
import org.monte.media.pgm.PGMImageReaderSpi

object SimpleTest {
  
  def main(args: Array[String]) {
    IIORegistry.getDefaultInstance().registerServiceProvider(new PGMImageReaderSpi)
    
    val cascade = Cascade.apply(Datasets.haarCascadeFrontalFaceDefault.getPath)
    val lena = getClass.getResource("/lena.jpg")
    val orl1 = getClass.getResource("/orl_faces/s9/1.pgm")
    
    val lenaImg = ImageIO.read(lena)
    val orl1Img = ImageIO.read(orl1)
    
    val detector = new SVJDetector(1f, 1.25f, .1f, 1, true)
    println("Lena returned: %s" format detector.detect(lenaImg, cascade))
    println("orl1 returned: %s" format detector.detect(orl1Img, cascade))
  }

}