/*
 * node2/src/main/scala/org/example/App.scala
 */
package org.example

import scala.jdk.CollectionConverters.*
import akka.actor.typed.receptionist.Receptionist.Command
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory

// Import from public API
import org.example.api.protocol.{Message, Ping, Pong}
import org.example.api.discovery.ServiceKeys

import scala.collection.immutable.HashMap

object WorkerOne {
  var pongSelfRef: Option[ActorRef[Message]] = Option.empty[ActorRef[Message]]

  def apply(): WorkerOne = new WorkerOne()
}

class WorkerOne {
  def behavior(): Behavior[Message] =
    Behaviors.setup { context =>
      WorkerOne.pongSelfRef = Option(context.self)
      context.system.receptionist ! Receptionist.Register(ServiceKeys.PongServiceKey, context.self)
      Behaviors.receiveMessagePartial(getReachableBehavior(context))
    }

  protected def getReachableBehavior(context: ActorContext[Message]): PartialFunction[Message, Behavior[Message]] =
    case Pong(replyTo) =>
      println("Pong received")
      replyTo ! Ping(context.self)
      println("Ping sent")
      Behaviors.same
}

object App:
  case object FindPing extends Message
  case object Start extends Message
  private case class ListingResponse(listing: Receptionist.Listing) extends Message
  
  private def initBasicConfig =
    var settings = new HashMap[String, Object]

    settings += ("akka.remote.artery.canonical.hostname" -> "worker2")
    settings += ("akka.remote.artery.canonical.port" -> "2551")
    settings += ("akka.remote.artery.bind.hostname" -> "0.0.0.0")
    settings += ("akka.remote.artery.bind.port" -> "2551")
    settings += ("akka.actor.allow-java-serialization" -> "on")
    settings += ("akka.remote.artery.transport" -> "tcp")
    settings += ("akka.cluster.seed-nodes" ->
      List("akka://akka-cluster-system@worker1:2555", "akka://akka-cluster-system@worker2:2551").asJava)
    settings += ("akka.cluster.downing-provider-class" -> "akka.cluster.sbr.SplitBrainResolverProvider")

    settings += ("akka.actor.provider" -> "cluster")

    ConfigFactory.parseMap(settings.asJava).withFallback(ConfigFactory.load())

  def main(args: Array[String]): Unit =
    var pingRef: ActorRef[Message] = null
    
    val system = ActorSystem(Behaviors.setup { ctx =>
      val listingResponseAdapter = ctx.messageAdapter[Receptionist.Listing](ListingResponse.apply)

      ctx.spawn(WorkerOne().behavior(), "WorkerOne")

      Behaviors.receiveMessage {
        case FindPing =>
          println("Finding ping actors...")
          ctx.system.receptionist ! Receptionist.Find(ServiceKeys.PingServiceKey, listingResponseAdapter)
          Behaviors.same
        case ListingResponse(ServiceKeys.PingServiceKey.Listing(listings)) =>
          println("Received pingService")
          if (listings.nonEmpty) pingRef = listings.head
          Behaviors.same
        case Start =>
          pingRef ! Ping(WorkerOne.pongSelfRef.get)
          Behaviors.same
      }
    }, "akka-cluster-system", initBasicConfig)

    val cluster = Cluster(system)

    while (pingRef == null) {
      system ! FindPing
      Thread.sleep(1000)
    }
    
    system ! Start
    println("Worker One executed successfully")