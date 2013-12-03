package edu.fit.cs.svj.par

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import javax.swing.JPanel
import java.text.DecimalFormat
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import com.sleepingdumpling.jvideoinput.VideoInput
import com.sleepingdumpling.jvideoinput.VideoFrame
import javax.swing.SwingUtilities
import java.util.concurrent.locks.LockSupport
import com.sleepingdumpling.jvideoinput.VideoInputException
import java.awt.GraphicsEnvironment
import java.awt.image.DataBufferInt
import java.awt.Transparency
import javax.swing.JFrame
import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.mutable.Map
import edu.fit.cs.svj.model.Rectangle
import java.awt.Color
import scala.concurrent.forkjoin.ThreadLocalRandom
import edu.fit.cs.svj.model.IntegralImage
import edu.fit.cs.svj.model.Cascade
import edu.fit.cs.svj.data.Datasets
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import java.util.concurrent.TimeoutException
import akka.actor.RootActorPath

class Demo(cam: WebCamPanel) extends Actor {
  private var masters = IndexedSeq.empty[ActorRef]
  private var image: Option[BufferedImage] = None

  implicit val timeout = Timeout(1 second)
  
  val cluster = Cluster(context.system)
  
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def processCam {
    val interval = 1000000000L / cam.fps

    while (true) {
      if (masters.isEmpty) {
        println("NO MASTERS DETECTED... waiting..")
        Thread.sleep(2000)
      } else {
        val start = System.currentTimeMillis
        try {
          cam.nextFrame match {
            case Some(frame) =>
              image = Some(frame.getBufferedImage)
              val master = masters(ThreadLocalRandom.current.nextInt(0, masters.size))
              val result = master ? Detect("", IntegralImage(image.get))
              val answer = Await.result(result, timeout.duration).asInstanceOf[Result]
              cam.update(image, answer.rectangles)
            case None => // no-op
          }
        } catch {
          case ex: TimeoutException =>
            println("FRAME TIMED OUT...")
        } finally {
          val end = System.nanoTime
          val waitTime = interval - (end - start)
          if (waitTime > 0) LockSupport.parkNanos(waitTime)
        }
      }
    }
  }

  def receive = {
    case MasterRegistration if !masters.contains(sender) =>
      println("MASTER ATTACHED...")
      context watch sender
      masters = masters :+ sender
      processCam
    case MasterRegistration =>
      println("NUM MASTERS = %d" format masters.size)
    case MemberUp(m) if m.hasRole("master") =>
      val master = context.actorSelection(RootActorPath(m.address) / "user" / "master")
      if (!masters.contains(master)) {
        masters = masters :+ Await.result(master.resolveOne, timeout.duration).asInstanceOf[ActorRef]
        println("NEW MASTER ATTACHED...")
        processCam
      }
    case _ =>
  }

}

class WebCamPanel(width: Int, height: Int, val fps: Int) extends JPanel {
  private val MILLIS = 1000000000L
  private var camInput: Option[VideoInput] = None
  private var displayImage: Option[(BufferedImage, List[Rectangle])] = None
  
  def nextFrame: Option[VideoFrame] = camInput match {
    case None => 
      camInput = Some(new VideoInput(width, height))
      nextFrame
    case Some(input) => input.getNextFrame(null) match {
      case frame: VideoFrame => Some(frame)
      case _ => None
    }
  }

  def update(img: Option[BufferedImage], rects: List[Rectangle]): Unit = img match {
    case Some(im) =>
      displayImage = Some((im, rects))
      SwingUtilities.invokeAndWait(new Runnable {
        def run(): Unit = paintImmediately(0, 0, getWidth, getHeight)
      })
    case None => // no interest
  }

  override def paint(g: Graphics) {
    super.paint(g)
    displayImage match {
      case Some((img, rects)) =>
        val gfx = g.create.asInstanceOf[Graphics2D]
        gfx.drawImage(img, 0, 0, null)
        gfx.setColor(Color.GREEN)
        rects foreach { r =>
          gfx.drawRect(r.x, r.y, r.width, r.height)
        }

        gfx.dispose
      case None => println("DISPLAY IMAGE WAS EMPTY")
    }
  }
}

object Demo {
  import edu.fit.cs.svj.common._

  def main(args: Array[String]) {
    val config = ConfigFactory.parseString("akka.cluster.roles = [webcam]").withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", config)

    val width = 640
    val height = 480
    val fps = 24

    println("CREATING USER INTERFACE")
    val frame = new JFrame
    val panel = new WebCamPanel(width, height, fps)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.getContentPane.add(panel)
    frame.setResizable(false)
    frame.pack()
    frame.setLocationRelativeTo(null)
    frame.setSize(width, height)

    system.actorOf(Props(classOf[Demo], panel), name = "webcam")

    frame.setVisible(true)
  }

}