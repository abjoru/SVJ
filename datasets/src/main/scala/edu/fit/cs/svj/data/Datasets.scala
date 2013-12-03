package edu.fit.cs.svj.data

import scala.io.Source
import scalax.chart.XYChart
import java.io.File
import scala.xml.XML
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

object Datasets {
  val ORL_DATA_FILE = "/orl_faces.csv"
  val HAAR_FF_ALT_FILE = "/haarcascades/haarcascade_frontalface_alt.xml"
  val HAAR_FF_ALT2_FILE = "/haarcascades/haarcascade_frontalface_alt2.xml"
  val HAAR_FF_DEF_FILE = "/haarcascades/haarcascade_frontalface_default.xml"

  lazy val orlFaces = getClass.getResource(ORL_DATA_FILE)
  lazy val haarCascadeFrontalFaceAlt = getClass.getResource(HAAR_FF_ALT_FILE)
  lazy val haarCascadeFrontalFaceAlt2 = getClass.getResource(HAAR_FF_ALT2_FILE)
  lazy val haarCascadeFrontalFaceDefault = getClass.getResource(HAAR_FF_DEF_FILE)

  def groups = Source.fromURL(orlFaces).getLines.toList.map { l =>
    val Array(v, k) = l split ";"
    (k, v)
  }.groupBy(_._1).map { case (k, v) => (k, v.map(_._2)) }

  def orlImages = Source.fromURL(orlFaces).getLines.toList.map { l =>
    l split ";" match {
      case Array(path, id, x, y) => new ImageData(id.toInt, path, Some(new Coord(x.toInt, y.toInt)))
      case Array(path, id) => new ImageData(id.toInt, path, None)
    }
  }.groupBy(_.id)

  def save(chart: XYChart, path: String, filename: String) {
    val root = new File("graphs")
    val subpath = new File(root, path)
    if (!subpath.exists()) subpath.mkdirs
    chart.saveAsPNG(new File(subpath, filename).getAbsolutePath, (1024, 768))
  }

  def saveAsXml(data: List[ResultData], path: String, filename: String) {
    val root = new File("graphs")
    val subpath = new File(root, path)
    if (!subpath.exists) subpath.mkdirs

    val xml = <comparison>
                {
                  data.map { res =>
                    <execution name={ res.cascade } time={ res.time.toString } averageDetectionRate={ res.averageDetection.toString }>
                      {
                        res.properties.map {
                          case (k, v) => <property name={ k } value={ v }/>
                        }
                      }
                      {
                        res.result.map {
                          case (k, v) => <item id={ k.toString } detected={ v._1.toString } total={ v._2.toString }/>

                        }
                      }
                    </execution>
                  }
                }
              </comparison>

    XML.save(new File(subpath, filename).getAbsolutePath, xml)
  }

}