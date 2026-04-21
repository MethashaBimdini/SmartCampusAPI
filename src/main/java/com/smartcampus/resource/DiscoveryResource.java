package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Part 1.2 - Discovery Endpoint
 *
 * GET /api/v1
 * Returns API metadata including versioning, contact info, and resource map.
 * This implements a basic HATEOAS (Hypermedia As The Engine Of Application State)
 * pattern, helping clients discover available resources dynamically.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, Object> response = new HashMap<>();

        // API Versioning Info
        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0");
        response.put("status", "operational");

        // Admin contact
        Map<String, String> contact = new HashMap<>();
        contact.put("name", "Campus Facilities Admin");
        contact.put("email", "w2120506@westminster.ac.uk");
        response.put("contact", contact);

        // Resource map (HATEOAS links) - helps clients discover endpoints
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        resources.put("sensorReadings", "/api/v1/sensors/{sensorId}/readings");
        response.put("resources", resources);

        // Available actions
        Map<String, Object> actions = new HashMap<>();
        actions.put("createRoom", "POST /api/v1/rooms");
        actions.put("listRooms", "GET /api/v1/rooms");
        actions.put("getRoomById", "GET /api/v1/rooms/{roomId}");
        actions.put("deleteRoom", "DELETE /api/v1/rooms/{roomId}");
        actions.put("createSensor", "POST /api/v1/sensors");
        actions.put("listSensors", "GET /api/v1/sensors?type={optional}");
        actions.put("getSensorById", "GET /api/v1/sensors/{sensorId}");
        actions.put("getSensorReadings", "GET /api/v1/sensors/{sensorId}/readings");
        actions.put("postSensorReading", "POST /api/v1/sensors/{sensorId}/readings");
        response.put("availableActions", actions);

        response.put("description",
            "RESTful API for managing campus rooms and IoT sensors. " +
            "Provides full CRUD operations with nested sub-resource support for sensor readings.");

        return Response.ok(response).build();
    }
}
