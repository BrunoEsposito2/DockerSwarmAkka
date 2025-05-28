/*
 * node1/src/main/scala/org/example/api/discovery/ServiceKeys.scala
 * node2/src/main/scala/org/example/api/discovery/ServiceKeys.scala
 */
package org.example.api.discovery

import akka.actor.typed.receptionist.ServiceKey
import org.example.api.protocol.Message

/**
 * Public API: Service discovery keys for Akka cluster service registration.
 * 
 * These service keys are used by cluster nodes to register and discover
 * services within the cluster. Changing these keys breaks compatibility
 * with existing deployments and requires a MAJOR version bump.
 * 
 * @since 1.0.0
 * @apiNote All service keys are part of the public API
 */
object ServiceKeys {

  /**
   * Public API: Service discovery key for Pong service registration.
   * 
   * Used by cluster nodes to register actors that can handle [[org.example.api.protocol.Pong]] 
   * messages. Other nodes use this key to discover and communicate with Pong service instances.
   * 
   * @since 1.0.0
   * @apiNote Changing this key name breaks service discovery - requires MAJOR version bump
   * @example
   * {{{
   * // Register a service
   * context.system.receptionist ! Receptionist.Register(ServiceKeys.PongServiceKey, context.self)
   * 
   * // Discover services  
   * context.system.receptionist ! Receptionist.Find(ServiceKeys.PongServiceKey, adapter)
   * }}}
   */
  val PongServiceKey: ServiceKey[Message] = ServiceKey[Message]("pongService")

  /**
   * Public API: Service discovery key for Ping service registration.
   * 
   * Used by cluster nodes to register actors that can handle [[org.example.api.protocol.Ping]]
   * messages. Other nodes use this key to discover and communicate with Ping service instances.
   * 
   * @since 1.0.0
   * @apiNote Changing this key name breaks service discovery - requires MAJOR version bump
   * @example
   * {{{
   * // Register a service
   * context.system.receptionist ! Receptionist.Register(ServiceKeys.PingServiceKey, context.self)
   * 
   * // Discover services
   * context.system.receptionist ! Receptionist.Find(ServiceKeys.PingServiceKey, adapter) 
   * }}}
   */
  val PingServiceKey: ServiceKey[Message] = ServiceKey[Message]("pingService")
}