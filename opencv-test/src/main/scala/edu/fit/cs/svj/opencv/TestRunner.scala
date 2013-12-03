package edu.fit.cs.svj.opencv

import org.opencv.core.MatOfRect
import org.opencv.objdetect.CascadeClassifier
import org.opencv.highgui.Highgui
import scala.io.Source
import org.opencv.core.Core
import scalax.chart._
import scalax.chart.Charting._
import scala.collection.mutable.ListBuffer
import java.net.URL
import edu.fit.cs.svj.data.Datasets
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import edu.fit.cs.svj.data.ResultData

object TestRunner {
  import edu.fit.cs.svj.common._

  def detect(img: String, detector: CascadeClassifier): MatOfRect = {
    val image = Highgui.imread(absPath(img.substring(1)))
    val result = new MatOfRect()

    detector.detectMultiScale(image, result)
    result
  }

  def run(cascade: String) = {
    println("[OpenCV] Running detection using classifier: %s" format cascade)
    val detector = new CascadeClassifier(absPath(cascade))
    val (result, time) = profile {
      Datasets.orlImages.foldLeft(Map[Int, (Int, Int)]()) { (m, items) =>
        m += items._1 -> (items._2.count { v =>
          val rects = detect(v.path, detector).toList().takeWhile { r =>
            v.coord match {
              case Some(c) => c.within(r.x, r.y, r.width, r.height)
              case None => true
            }
          }

          rects.size > 0
        }, items._2.size)
      }
    }

    val avg = result.values.foldLeft[Int](0)((s, v) => s + v._1).toDouble / result.size
    val properties = Map[String, String]()

    ResultData(cascade, avg, time, properties, result)
  }

  def main(args: Array[String]) {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    val r1 = run(Datasets.HAAR_FF_ALT_FILE)
    val r2 = run(Datasets.HAAR_FF_ALT2_FILE)
    val r3 = run(Datasets.HAAR_FF_DEF_FILE)

    val xy1 = r1.result.view.map(e => (e._1, e._2._1)) toList
    val xy2 = r2.result.view.map(e => (e._1, e._2._1)) toList
    val xy3 = r3.result.view.map(e => (e._1, e._2._1)) toList

    val dataset = List(xy1.toXYSeries("ff alt", true, false),
      xy2.toXYSeries("ff alt2", true, false),
      xy3.toXYSeries("ff def", true, false))

    val chart = XYLineChart(dataset.toXYSeriesCollection, title = "OpenCV Cascade Comparator")

    Datasets.save(chart, "opencv", "cascade_compare.png")
    Datasets.saveAsXml(List(r1, r2, r3), "opencv", "comparison.xml")
    chart.show
  }

}