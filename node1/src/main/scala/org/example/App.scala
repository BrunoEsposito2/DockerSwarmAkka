package org.example

import scala.jdk.CollectionConverters.*
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import org.example.*
import org.example.WorkerTwo.pingSelfRef

import java.util.logging.LogManager
import scala.collection.immutable.HashMap

trait Message
final case class Pong(replyTo: ActorRef[Ping]) extends Message
final case class Ping(replyTo: ActorRef[Pong]) extends Message

val PongServiceKey = ServiceKey[Message]("pongService")
val PingServiceKey = ServiceKey[Message]("pingService")

object WorkerTwo {
  var pingSelfRef: Option[ActorRef[Message]] = Option.empty[ActorRef[Message]]
  def apply(): WorkerTwo = new WorkerTwo()
}

class WorkerTwo {
  def behavior(): Behavior[Message] =
    Behaviors.setup { context =>
      WorkerTwo.pingSelfRef = Option(context.self)
      context.system.receptionist ! Receptionist.Register(PingServiceKey, context.self)
      Behaviors.receiveMessagePartial(getReachableBehavior(context))
    }

  protected def getReachableBehavior(context: ActorContext[Message]): PartialFunction[Message, Behavior[Message]] =
    case Ping(replyTo) =>
      println("Ping received")
      replyTo ! Pong(context.self)
      println("Pong sent")
      Behaviors.same
}

object App:
  case object FindPong extends Message
  case object Start extends Message
  private case class ListingResponse(listing: Receptionist.Listing) extends Message

  private def initBasicConfig =
    var settings = new HashMap[String, Object]

    settings += ("akka.remote.artery.canonical.hostname" -> "worker1")
    settings += ("akka.remote.artery.canonical.port" -> "2555")
    settings += ("akka.actor.allow-java-serialization" -> "on")
    settings += ("akka.remote.artery.transport" -> "tcp")
    settings += ("akka.cluster.seed-nodes" ->
      List("akka://akka-cluster-system@worker1:2555", "akka://akka-cluster-system@worker2:2551").asJava)
    settings += ("akka.cluster.downing-provider-class" -> "akka.cluster.sbr.SplitBrainResolverProvider")

    settings += ("akka.actor.provider" -> "cluster")

    ConfigFactory.parseMap(settings.asJava).withFallback(ConfigFactory.load())

  def main(args: Array[String]): Unit =
    var pongRef: ActorRef[Message] = null
  
      val system = ActorSystem(Behaviors.setup { ctx =>
        val listingResponseAdapter = ctx.messageAdapter[Receptionist.Listing](ListingResponse.apply)
  
        ctx.spawn(WorkerTwo().behavior(), "WorkerTwo")
  
        Behaviors.receiveMessage {
          case FindPong =>
            println("Finding pong actors...")
            ctx.system.receptionist ! Receptionist.Find(ServiceKey[Message]("pongService"), listingResponseAdapter)
            Behaviors.same
          case ListingResponse(PongServiceKey.Listing(listings)) =>
            println("Received pongService")
            if (listings.nonEmpty) pongRef = listings.head
            Behaviors.same
          case Start =>
            pongRef ! Pong(WorkerTwo.pingSelfRef.get)
            Behaviors.same
        }
      }, "akka-cluster-system", ConfigFactory.load())
  
      val cluster = Cluster(system)
  
      while (pongRef == null) {
        system ! FindPong
        Thread.sleep(1000)
      }
  
      system ! Start
      println("Worker Two executed successfully")
