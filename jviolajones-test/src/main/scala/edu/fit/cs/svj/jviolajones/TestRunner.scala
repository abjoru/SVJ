package edu.fit.cs.svj.jviolajones

import java.net.URL
import jviolajones.Detector
import scala.collection.mutable.ListBuffer
import edu.fit.cs.svj.data.Datasets
import javax.imageio.spi.IIORegistry
import org.monte.media.pgm.PGMImageReaderSpi
import scalax.chart._
import scalax.chart.Charting._
import edu.fit.cs.svj.common._
import javax.imageio.ImageIO
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import edu.fit.cs.svj.data.ResultData

object TestRunner {
  val scale = 1f
  val scaleInc = 1.25f
  val increment = .1f
  val minNeighbors = 1
  val cannyPruning = true

  def run(cascade: String) = {
    println("[JViolaJones] Running detector using classifier: %s" format cascade)
    val detector = new Detector(absPath(cascade))
    
    val (result, time) = profile {
      Datasets.orlImages.foldLeft(Map[Int, (Int, Int)]()) { (m, items) =>
        m += items._1 -> (items._2.count { v =>
          val img = absPath(v.path.substring(1))
          val rects = detector.getFaces(img, scale, scaleInc, increment, minNeighbors, cannyPruning).takeWhile { r =>
            v.coord match {
              case Some(c) => c.within(r.getX.toInt, r.getY.toInt, r.getWidth.toInt, r.getHeight.toInt)
              case None => true
            }
          }

          rects.size > 0
        }, items._2.size)
      }
    }

    val avg = result.values.foldLeft[Int](0)((s, v) => s + v._1).toDouble / result.size
    val properties = Map(
      "scale" -> scale.toString,
      "scaleInc" -> scaleInc.toString,
      "increment" -> increment.toString,
      "minNeighbors" -> minNeighbors.toString,
      "cannyPruning" -> cannyPruning.toString)

    ResultData(cascade, avg, time, properties, result)
  }

  def main(args: Array[String]) {
    IIORegistry.getDefaultInstance().registerServiceProvider(new PGMImageReaderSpi())
    
    val r1 = run(Datasets.HAAR_FF_ALT_FILE)
    val r2 = run(Datasets.HAAR_FF_ALT2_FILE)
    val r3 = run(Datasets.HAAR_FF_DEF_FILE)

    val xyData1 = r1.result.view.map(e => (e._1, e._2._1)).toList
    val xyData2 = r2.result.view.map(e => (e._1, e._2._1)).toList
    val xyData3 = r3.result.view.map(e => (e._1, e._2._1)).toList
    
    val dataset = List(xyData1.toXYSeries("ff alt", true, false),
      xyData2.toXYSeries("ff alt2", true, false),
      xyData3.toXYSeries("ff def", true, false))
      
    val title = "JViolaJones [s=%s, si=%s, inc=%s, mn=%d, canny=%s]" format (scale, scaleInc, increment, minNeighbors, cannyPruning)
    val chart = XYLineChart(dataset.toXYSeriesCollection, title = title)

    Datasets.save(chart, "jviolajones", "cascade_compare.png")
    Datasets.saveAsXml(List(r1, r2, r3), "jviolajones", "comparison.xml")
    chart.show
  }

}