package org.example.api.router

import akka.actor.typed.ActorRef
import io.vertx.core.{Future, Vertx}
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.{BodyHandler, CorsHandler}
import io.vertx.core.http.{HttpMethod, HttpServer}

import java.util

// Swagger annotations
import io.swagger.v3.oas.annotations._
import io.swagger.v3.oas.annotations.media._
import io.swagger.v3.oas.annotations.parameters._
import io.swagger.v3.oas.annotations.responses._
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.annotations.info.{Info, Contact}
import javax.ws.rs.{GET, POST, Path, Produces, Consumes}
import javax.ws.rs.core.MediaType


object VertxRouter:
  def apply(): VertxRouter = new VertxRouter()

/**
 * Public API: HTTP Router implementation for camera management REST endpoints
 */
private class VertxRouter:
  private val vertx = Vertx.vertx()
  private val httpServer = vertx.createHttpServer()
  private var currentCameraId: String = "camera1" // Default camera ID
  private var subscribeStatus: String = "pending"
  private var inputStatus: String = "pending"
  private var configStatus: String = "pending"
  private var cameraMap: Map[String, ActorRef[_]] = Map.empty
  private var cameraNames: Map[String, String] = Map.empty
  private var serverRef: Option[ActorRef[_]] = None
  private var windowData: Map[String, Double] = Map.empty

  private var detectedCount: Int = 0
  private var detectionMode: String = "Initializing..."
  private var frameRate: Double = 0.0

  /**
   * Public API: Update detection data from camera processing
   * 
   * @param count Number of detected objects
   * @param mode Current detection mode
   * @param fps Frame rate of processing
   */
  def updateDetectionData(count: Int, mode: String, fps: Double): Unit = {
    detectedCount = count
    detectionMode = mode
    frameRate = fps
  }

  /**
   * Public API: Initialize HTTP routes and start server
   * 
   * Creates REST endpoints for camera management and system monitoring.
   * The server listens on port 4000 with CORS enabled.
   * 
   * @return Future containing the HTTP server instance
   */
  def initRoutes(): Future[HttpServer] =
    val router = Router.router(vertx)

    // Configurazione CORS
    val allowedHeaders = new util.HashSet[String]()
    allowedHeaders.add("Access-Control-Allow-Headers")
    allowedHeaders.add("Access-Control-Allow-Origin")
    allowedHeaders.add("Origin")
    allowedHeaders.add("X-Requested-With")
    allowedHeaders.add("Content-Type")
    allowedHeaders.add("Accept")

    val allowedMethods = new util.HashSet[HttpMethod]()
    allowedMethods.add(HttpMethod.GET)
    allowedMethods.add(HttpMethod.POST)
    allowedMethods.add(HttpMethod.OPTIONS)

    router.route().handler(
      CorsHandler.create()
        .addOrigin("*")
        .allowedHeaders(allowedHeaders)
        .allowedMethods(allowedMethods)
    )

    router.route().handler(BodyHandler.create())

    // Pre-flight OPTIONS request
    router.options().handler(ctx => {
      ctx.response()
        .putHeader("Access-Control-Allow-Origin", "*")
        .putHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS")
        .putHeader("Access-Control-Allow-Headers", "Content-Type")
        .end()
    })

    setupCameraSwitchEndpoint(router)
    setupWindowSelectionEndpoint(router)
    setupStatusEndpoint(router)

    // Avvia il server
    httpServer.requestHandler(router).listen(4000).onComplete(result => {
      if (result.succeeded()) {
        println(s"HTTP server running on port 4000")
      } else {
        println(s"Failed to start server: ${result.cause().getMessage}")
      }
    })

  /**
   * Public API: Switch active camera endpoint
   */
  private def setupCameraSwitchEndpoint(router: Router): Unit = {
    router.post("/camera/switch").handler(ctx => {
      try {
        val body = ctx.body().asJsonObject()
        currentCameraId = body.getString("cameraId")

        ctx.response()
          .putHeader("Content-Type", "application/json")
          .putHeader("Access-Control-Allow-Origin", "*")
          .end(new JsonObject()
            .put("cameraId", currentCameraId)
            .encode())
      } catch {
        case e: Exception =>
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .putHeader("Access-Control-Allow-Origin", "*")
            .setStatusCode(500)
            .end(new JsonObject()
              .put("error", e.getMessage)
              .encode())
      }
    })
  }

  /**
   * Public API: Window selection endpoint
   */
  private def setupWindowSelectionEndpoint(router: Router): Unit = {
    router.post("/window").handler(ctx => {
      val response = ctx.response()
        .putHeader("Content-Type", "application/json")
        .putHeader("Access-Control-Allow-Origin", "*")

      try {
        val body = ctx.body().asJsonObject()

        val requiredFields = List("x", "y", "width", "height")
        if (!requiredFields.forall(body.containsKey)) {
          windowData = Map.empty
        } else {
            val x = body.getInteger("x")
            val y = body.getInteger("y")
            val width = body.getInteger("width")
            val height = body.getInteger("height")

            if (x < 0 || y < 0 || width <= 0 || height <= 0) {
              throw new IllegalArgumentException("Invalid coordinates or dimensions")
            }

            windowData = Map(
              "startX" -> x.toDouble,
              "startY" -> y.toDouble,
              "width" -> width.toDouble,
              "height" -> height.toDouble
            )
        }
        
        response
          .end(new JsonObject()
            .put("status", "approved")
            .encode())
      } catch {
        case e: Exception =>
          if (!response.ended()) {
            response.setStatusCode(500)
              .end(new JsonObject().put("error", e.getMessage).encode())
          }
      }
    })
  }

  /**
   * Public API: System status endpoint
   */
  private def setupStatusEndpoint(router: Router): Unit = {
    router.get("/status").handler(ctx => {
      try {
        val camerasArray = new JsonArray()
        
        ctx.response()
          .putHeader("Content-Type", "application/json")
          .putHeader("Access-Control-Allow-Origin", "*")
          .end(new JsonObject()
            .put("subscribe", subscribeStatus)
            .put("input", inputStatus)
            .put("config", configStatus)
            .put("currentCamera", currentCameraId)
            .put("cameras", camerasArray)
            .put("peopleCount", detectedCount)
            .put("mode", detectionMode)
            .put("fps", frameRate)
            .encode())
      } catch {
        case e: Exception =>
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .putHeader("Access-Control-Allow-Origin", "*")
            .setStatusCode(500)
            .end(new JsonObject()
              .put("error", e.getMessage)
              .encode())
      }
    })
  }

  // Helper methods (internal, non-API)
  private def getFriendlyNameForCamera(cameraId: String): String =
    cameraId match {
      case "camera1" => "Main Entrance"
      case "camera2" => "Parking Area"
      case "camera3" => "Living Room"
      case _ => s"Camera ${cameraId.substring(6)}"
    }

  private def getLocationForCamera(cameraId: String): String =
    cameraId match {
      case "camera1" => "Front"
      case "camera2" => "Exterior"
      case "camera3" => "Indoor"
      case _ => "General"
    }

  def setServerRef(ref: ActorRef[_]): Unit = serverRef = Option(ref)
  def getCameraMap: Map[String, ActorRef[_]] = cameraMap
  def getCurrentCameraId: String = currentCameraId

  def updateServiceStatus(service: String, status: String): Unit =
    service match
      case "subscribe" => subscribeStatus = status
      case "input" => inputStatus = status
      case "config" => configStatus = status

  // Placeholder methods - rimuovi le dipendenze da tipi esterni
  /*
  def updateCameraMap(cameras: Map[Info, ChildStatuses], info: Info): Unit = {
    // Implementation removed - depends on external types
  }

  private def setCurrentCamera(info: Info): Unit = {
    // Implementation removed - depends on external types
  }

  private def addNewCameras(cameras: Map[Info, ChildStatuses]): Unit = {
    // Implementation removed - depends on external types
  }

  private def enumerateMap(cameras: Map[Info, ChildStatuses]): Unit = {
    // Implementation removed - depends on external types
  }
  */

/**
 * Swagger API Models - Request/Response schemas
 */
case class CameraSwitchRequest(
  @Schema(description = "ID of the camera to switch to", example = "camera1", pattern = "^camera\\d+$")
  cameraId: String
)

case class CameraSwitchResponse(
  @Schema(description = "ID of the currently active camera", example = "camera1")
  cameraId: String
)

@Schema(description = "Window coordinates request")
case class WindowRequest(
  @Schema(description = "X coordinate of top-left corner", example = "100", minimum = "0")
  x: Int,
  @Schema(description = "Y coordinate of top-left corner", example = "50", minimum = "0")
  y: Int,
  @Schema(description = "Width of the detection window", example = "200", minimum = "1")
  width: Int,
  @Schema(description = "Height of the detection window", example = "150", minimum = "1")
  height: Int
)

@Schema(description = "Window coordinates response")
case class WindowResponse(
  @Schema(description = "Status of the operation", example = "approved", allowableValues = Array("approved", "rejected"))
  status: String
)

@Schema(description = "System status response")
case class StatusResponse(
  @Schema(description = "Subscribe service status", example = "active", allowableValues = Array("pending", "active", "failed"))
  subscribe: String,
  @Schema(description = "Input service status", example = "active", allowableValues = Array("pending", "active", "failed"))
  input: String,
  @Schema(description = "Config service status", example = "active", allowableValues = Array("pending", "active", "failed"))
  config: String,
  @Schema(description = "Current active camera ID", example = "camera1")
  currentCamera: String,
  @Schema(description = "List of available cameras")
  cameras: Array[String],
  @Schema(description = "Number of detected people", example = "3", minimum = "0")
  peopleCount: Int,
  @Schema(description = "Current detection mode", example = "Active Detection")
  mode: String,
  @Schema(description = "Current frame rate", example = "30.5", minimum = "0")
  fps: Double
)

@Schema(description = "Error response")
case class ErrorResponse(
  @Schema(description = "Error message", example = "Invalid camera ID")
  error: String
)