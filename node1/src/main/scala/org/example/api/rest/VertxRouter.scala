package org.example.api.router

import akka.actor.typed.ActorRef
import io.vertx.core.{Future, Vertx}
import io.vertx.core.json.JsonObject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.{BodyHandler, CorsHandler}
import io.vertx.core.http.{HttpMethod, HttpServer}
import org.example.api.protocol.Message  // Import corretto per Message

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

/**
 * Public API: Vert.x HTTP Router for Akka Cluster Camera Management System
 * 
 * This router provides REST endpoints for managing camera switching, window selection,
 * and system status monitoring in the distributed Akka camera system.
 * 
 * @since 1.0.0
 * @apiNote This is part of the public HTTP API - endpoint changes require MAJOR version bump
 */
@OpenAPIDefinition(
  info = new Info(
    title = "Akka Cluster Camera Management API",
    version = "1.0.0",
    description = """REST API for camera management and system monitoring in the Akka cluster system.
    
    This API provides endpoints for:
    - Switching between available cameras
    - Setting detection window coordinates  
    - Monitoring system status and detection data
    
    The API runs on port 4000 with CORS enabled for cross-origin requests.""",
    contact = new Contact(
      name = "Akka Cluster Project",
      url = "https://github.com/brunoesposito2/DockerSwarmAkka"
    )
  ),
  servers = Array(
    new Server(url = "http://localhost:4000", description = "Development server"),
    new Server(url = "http://worker1:4000", description = "Production Node1"),
    new Server(url = "http://worker2:4000", description = "Production Node2")
  ),
  tags = Array(
    new Tag(name = "Camera Management", description = "Operations for camera control and switching"),
    new Tag(name = "Detection", description = "Window selection and detection configuration"),
    new Tag(name = "Monitoring", description = "System status and health monitoring")
  )
)
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
  private var cameraMap: Map[String, ActorRef[Message]] = Map.empty
  private var cameraNames: Map[String, String] = Map.empty
  private var serverRef: Option[ActorRef[Message]] = None
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
  @Path("/camera/switch")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(
    summary = "Switch Active Camera",
    description = """Changes the currently active camera in the cluster system.
    
    The camera ID should match one of the available cameras in the system.
    Valid camera IDs are typically: camera1, camera2, camera3, etc.
    
    After switching, the system will begin processing video from the new camera.""",
    tags = Array("Camera Management"),
    requestBody = new RequestBody(
      description = "Camera switch request with target camera ID",
      required = true,
      content = Array(new Content(
        mediaType = MediaType.APPLICATION_JSON,
        schema = new Schema(implementation = classOf[CameraSwitchRequest]),
        examples = Array(
          new ExampleObject(name = "switch_to_camera1", summary = "Switch to main entrance", 
            value = """{"cameraId": "camera1"}"""),
          new ExampleObject(name = "switch_to_camera2", summary = "Switch to parking area", 
            value = """{"cameraId": "camera2"}""")
        )
      ))
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Camera switched successfully",
        content = Array(new Content(
          mediaType = MediaType.APPLICATION_JSON,
          schema = new Schema(implementation = classOf[CameraSwitchResponse])
        ))
      ),
      new ApiResponse(
        responseCode = "500",
        description = "Internal server error or invalid camera ID",
        content = Array(new Content(
          mediaType = MediaType.APPLICATION_JSON,
          schema = new Schema(implementation = classOf[ErrorResponse])
        ))
      )
    )
  )
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
  @Path("/window")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(
    summary = "Set Detection Window",
    description = """Sets the detection window coordinates for camera processing.
    
    The window defines the area within the camera frame where object detection
    will be performed. Coordinates are in pixels relative to the video frame.
    
    Requirements:
    - x, y: Top-left corner coordinates (must be >= 0)
    - width, height: Window dimensions (must be > 0)
    
    If invalid coordinates are provided, the detection window will be reset.""",
    tags = Array("Detection"),
    requestBody = new RequestBody(
      description = "Window coordinates in pixels",
      required = true,
      content = Array(new Content(
        mediaType = MediaType.APPLICATION_JSON,
        schema = new Schema(implementation = classOf[WindowRequest]),
        examples = Array(
          new ExampleObject(name = "center_window", summary = "Center detection window", 
            value = """{"x": 200, "y": 150, "width": 400, "height": 300}"""),
          new ExampleObject(name = "full_frame", summary = "Full frame detection", 
            value = """{"x": 0, "y": 0, "width": 1920, "height": 1080}""")
        )
      ))
    ),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Window coordinates set successfully",
        content = Array(new Content(
          mediaType = MediaType.APPLICATION_JSON,
          schema = new Schema(implementation = classOf[WindowResponse])
        ))
      ),
      new ApiResponse(
        responseCode = "500",
        description = "Invalid coordinates or internal error",
        content = Array(new Content(
          mediaType = MediaType.APPLICATION_JSON,
          schema = new Schema(implementation = classOf[ErrorResponse])
        ))
      )
    )
  )
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
  @Path("/status")
  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(
    summary = "Get System Status",
    description = """Returns current system status including:
    - Service statuses (subscribe, input, config)
    - Current active camera information
    - Available cameras list
    - Real-time detection data (people count, mode, FPS)
    
    This endpoint can be polled regularly to monitor system health and detection performance.""",
    tags = Array("Monitoring"),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "System status retrieved successfully",
        content = Array(new Content(
          mediaType = MediaType.APPLICATION_JSON,
          schema = new Schema(implementation = classOf[StatusResponse]),
          examples = Array(
            new ExampleObject(name = "active_system", summary = "System with active detection", 
              value = """{"subscribe": "active", "input": "active", "config": "active", "currentCamera": "camera1", "cameras": ["camera1", "camera2"], "peopleCount": 3, "mode": "Active Detection", "fps": 30.5}"""),
            new ExampleObject(name = "initializing", summary = "System initializing", 
              value = """{"subscribe": "pending", "input": "pending", "config": "active", "currentCamera": "camera1", "cameras": [], "peopleCount": 0, "mode": "Initializing...", "fps": 0.0}""")
          )
        ))
      ),
      new ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = Array(new Content(
          mediaType = MediaType.APPLICATION_JSON,
          schema = new Schema(implementation = classOf[ErrorResponse])
        ))
      )
    )
  )
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

  def setServerRef(ref: ActorRef[Message]): Unit = serverRef = Option(ref)
  def getCameraMap: Map[String, ActorRef[Message]] = cameraMap
  def getCurrentCameraId: String = currentCameraId

  def updateServiceStatus(service: String, status: String): Unit =
    service match
      case "subscribe" => subscribeStatus = status
      case "input" => inputStatus = status
      case "config" => configStatus = status

  // Placeholder methods - rimuovi le dipendenze da tipi esterni
  // Se hai bisogno di questi tipi, creali oppure rimuovi questi metodi
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
@Schema(description = "Camera switch request")
case class CameraSwitchRequest(
  @Schema(description = "ID of the camera to switch to", example = "camera1", pattern = "^camera\\d+$")
  cameraId: String
)

@Schema(description = "Camera switch response")
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