package edu.fit.cs.svj.par

import akka.actor._
import akka.routing.SmallestMailboxRouter
import akka.routing.Broadcast
import edu.fit.cs.svj.model.IntegralImage
import com.typesafe.config.ConfigFactory
import akka.routing.FromConfig
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.Member
import edu.fit.cs.svj.model.Cascade
import edu.fit.cs.svj.common._
import edu.fit.cs.svj.data.Datasets

/**
 * Parallel level: Haar Cascade
 * TODO make similar using window size (scale) as parallel level
 */
class CascadeMaster(cascade: Cascade, scale: (Float, Float), inc: Float, minNeighbors: Int) extends Actor {

  val cluster = Cluster(context.system)
  val router = context.actorOf(FromConfig.props(Props[CascadeWorker]), name = "workerRouter")

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
    router ! Broadcast(Init(cascade, scale._1, scale._2, inc, minNeighbors))
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case Detect(id, image) =>
      router ! Work(id, scale._1, Nil, image, sender)
    //println("RECEIVED WORK REQUEST...")
    case w: Work =>
      router ! w
    //println("RECEIVED SUB-WORK REQUEST...")
    case m: Merge =>
      router ! m
    //println("RECEIVED MERGE REQUEST...")
    case MemberUp(m) => register(m)
  }

  def register(member: Member): Unit = member match {
    case m if m.hasRole("webcam") => actorPath(m, "webcam") ! MasterRegistration
    case m if m.hasRole("client") => actorPath(m, "simpleClient") ! MasterRegistration
    case _ => // not iterested
  }

  private def actorPath(m: Member, pathTo: String) =
    context.actorSelection(RootActorPath(m.address) / "user" / pathTo)
}

trait CascadeRunner {
  val scale = (5f, 1.25f) //(1f, 1.25f)
  val inc = .1f
  val minNeighbors = 2 //1
    
  def config(args: Array[String]) = (
    if (args.nonEmpty) ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}")
    else ConfigFactory.empty
  ).withFallback(ConfigFactory.parseString("akka.cluster.roles = [master]")).withFallback(ConfigFactory.load())
}

object CascadeMasterFrontalFaceAlt extends CascadeRunner {

  def main(args: Array[String]) {
    val cascade = Cascade(absPath(Datasets.HAAR_FF_ALT_FILE))
    val system = ActorSystem("ClusterSystem", config(args))
    system.actorOf(Props(classOf[CascadeMaster], cascade, scale, inc, minNeighbors), name = "master")
  }

}

object CascadeMasterFrontalFaceAlt2 extends CascadeRunner {

  def main(args: Array[String]) {
    val cascade = Cascade(absPath(Datasets.HAAR_FF_ALT2_FILE))
    val system = ActorSystem("ClusterSystem", config(args))
    system.actorOf(Props(classOf[CascadeMaster], cascade, scale, inc, minNeighbors), name = "master")
  }

}

object CascadeMasterFrontalFaceDefault extends CascadeRunner {

  def main(args: Array[String]) {
    val cascade = Cascade(absPath(Datasets.HAAR_FF_DEF_FILE))
    val system = ActorSystem("ClusterSystem", config(args))
    system.actorOf(Props(classOf[CascadeMaster], cascade, scale, inc, minNeighbors), name = "master")
  }

}