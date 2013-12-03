package edu.fit.cs.svj.par

import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.Cluster
import akka.actor.Actor
import akka.actor.RelativeActorPath
import akka.actor.Address
import akka.cluster.ClusterEvent._
import edu.fit.cs.svj.data.ImageData
import scala.concurrent.forkjoin.ThreadLocalRandom
import akka.actor.RootActorPath
import com.typesafe.config.ConfigFactory
import edu.fit.cs.svj.data.Datasets
import akka.actor.RootActorPath
import edu.fit.cs.svj.model.Rectangle
import edu.fit.cs.svj.model.Cascade
import javax.imageio.ImageIO
import edu.fit.cs.svj.common._
import akka.actor.ActorRef
import edu.fit.cs.svj.model.IntegralImage
import javax.imageio.spi.IIORegistry
import org.monte.media.pgm.PGMImageReaderSpi
import java.util.concurrent.TimeUnit

case class ImageDetection(path: String, rectangles: List[Rectangle] = Nil)
case class Images(imgList: List[ImageDetection], cascade: Cascade, scale: (Float, Float), increment: Float)

class SimpleClient extends Actor {
  import scala.concurrent.duration._
  
  var masters = IndexedSeq.empty[ActorRef]
  val timer = new Timer

  def receive = {
    case MasterRegistration if !masters.contains(sender) =>
      context watch sender
      masters = masters :+ sender
      println("REGISTERED WITH MASTER!! master list: %d" format masters.size)

    case Images(imgs, cascade, scale, increment) if masters.nonEmpty =>
      timer.start
      imgs foreach { img =>
        val master = masters(ThreadLocalRandom.current.nextInt(0, masters.size))
        val image = ImageIO.read(getClass.getResource(img.path))
        master ! Detect(img.path, IntegralImage(image))
      }
    case _: Images =>
      println("No available nodes to use, please try again later")
    case Result(id, rects) =>
      //val duration = Duration(timer.stop, TimeUnit.MILLISECONDS)
      val time = timer.stop
      //println(s"Image [${id}] returned ${rects}")
      println("TOOK %s ms" format time)
  }
}

object SimpleClient {

  def main(args: Array[String]) {
    IIORegistry.getDefaultInstance().registerServiceProvider(new PGMImageReaderSpi)

    val config = ConfigFactory.parseString("akka.cluster.roles = [client]").withFallback(ConfigFactory.load("svj"))

    val system = ActorSystem("ClusterSystem", config)
    //system.log.info("SimpleClient will start when at least one member in the cluster")
    val client = system.actorOf(Props(classOf[SimpleClient]), name = "simpleClient")
    Thread.sleep(2000)
    //client ! Images(List(ImageDetection("/lena.jpg")), Cascade(absPath(Datasets.HAAR_FF_ALT2_FILE)), (1f, 1.25f), .1f)

    val images = for {
      item <- Datasets.orlImages
      image <- item._2
    } yield ImageDetection(image.path.substring(1))

    client ! Images(images.toList, Cascade(absPath(Datasets.HAAR_FF_DEF_FILE)), (1f, 1.25f), .1f)

    /*
    Cluster(system) registerOnMemberUp {
      val client = system.actorOf(Props(classOf[SimpleClient]), name = "simpleClient")
      client ! Images(List(ImageDetection("/lena.jpg")), Cascade(absPath(Datasets.HAAR_FF_ALT2_FILE)), (1f, 1.25f), .1f)
    }*/
  }
}