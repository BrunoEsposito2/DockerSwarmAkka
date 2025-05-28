/*
 * node1/src/main/scala/org/example/api/protocol/Messages.scala
 * node2/src/main/scala/org/example/api/protocol/Messages.scala
 */
package org.example.api.protocol

import akka.actor.typed.ActorRef

/**
 * Public API: Base trait for all messages in the Akka cluster communication protocol.
 * 
 * This trait defines the foundation for message passing between cluster nodes.
 * Any modifications to this trait or its implementations constitute breaking 
 * changes requiring a MAJOR version bump according to Semantic Versioning.
 * 
 * @since 1.0.0
 * @apiNote This is part of the public API - changes require MAJOR version bump
 */
trait Message

/**
 * Public API: Pong response message in the ping-pong communication protocol.
 * 
 * Sent as a response to a [[Ping]] message. Contains a reference to the actor
 * that should receive the corresponding [[Ping]] message to continue the
 * ping-pong cycle.
 * 
 * @param replyTo Actor reference to send the corresponding Ping message back to
 * @since 1.0.0
 * @apiNote Part of public message protocol - signature changes require MAJOR version bump
 * @example
 * {{{
 * // Responding to a ping with a pong
 * case Ping(replyTo) =>
 *   replyTo ! Pong(context.self)
 * }}}
 */
final case class Pong(replyTo: ActorRef[Ping]) extends Message

/**
 * Public API: Ping request message in the ping-pong communication protocol.
 * 
 * Initiates a ping-pong cycle by sending this message to a remote actor.
 * The receiving actor should respond with a [[Pong]] message containing
 * a reference back to the original sender.
 * 
 * @param replyTo Actor reference to send the corresponding Pong message back to
 * @since 1.0.0
 * @apiNote Part of public message protocol - signature changes require MAJOR version bump
 * @example
 * {{{
 * // Starting a ping-pong cycle
 * pongService ! Ping(context.self)
 * }}}
 */
final case class Ping(replyTo: ActorRef[Pong]) extends Message