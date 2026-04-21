package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Part 3 - Sensor Operations & Linking
 *
 * Manages /api/v1/sensors
 *
 * POST   /sensors              - Register new sensor (validates roomId exists)
 * GET    /sensors              - List all sensors (optional ?type= filter)
 * GET    /sensors/{sensorId}   - Get a specific sensor
 * GET    /sensors/{sensorId}/readings - Sub-resource locator (Part 4)
 * POST   /sensors/{sensorId}/readings - Sub-resource locator (Part 4)
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * POST /api/v1/sensors
     *
     * Registers a new sensor. Validates that the roomId in the request body
     * actually exists in the system. If not, throws LinkedResourceNotFoundException
     * which is mapped to HTTP 422 Unprocessable Entity.
     *
     * Uses @Consumes(APPLICATION_JSON): If a client sends text/plain or
     * application/xml, JAX-RS returns 415 Unsupported Media Type automatically.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(buildError("Sensor ID is required"))
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(buildError("roomId is required"))
                    .build();
        }

        // Foreign Key Validation: roomId must exist
        if (!dataStore.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor. Room with ID '" + sensor.getRoomId() + "' does not exist."
            );
        }

        if (dataStore.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(buildError("Sensor with ID '" + sensor.getId() + "' already exists"))
                    .build();
        }

        // Default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        dataStore.addSensor(sensor);

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location)
                .entity(sensor)
                .build();
    }

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     *
     * Lists all sensors. Supports optional @QueryParam "type" for filtering.
     * Using @QueryParam is preferred over path-based filtering (e.g., /sensors/type/CO2)
     * because query parameters are semantically correct for filtering/searching collections.
     * Path segments should represent resources, not filter operations.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(dataStore.getSensors().values());

        if (type != null && !type.trim().isEmpty()) {
            sensorList = sensorList.stream()
                    .filter(s -> s.getType() != null &&
                                 s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensorList).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns details of a specific sensor.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensorById(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(buildError("Sensor not found with ID: " + sensorId))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * Part 4.1 - Sub-Resource Locator Pattern
     *
     * /api/v1/sensors/{sensorId}/readings
     *
     * This method does NOT handle the request itself — it delegates to
     * SensorReadingResource. JAX-RS will inject the sensorId context and
     * forward GET/POST calls on /readings to that dedicated class.
     *
     * Benefits: Separation of concerns, better maintainability, and the ability
     * to independently test reading logic without touching the sensor controller.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        // Verify sensor exists before delegating
        Sensor sensor = dataStore.getSensorById(sensorId);
        if (sensor == null) {
            throw new javax.ws.rs.NotFoundException("Sensor not found with ID: " + sensorId);
        }
        return new SensorReadingResource(sensorId);
    }

    private java.util.Map<String, String> buildError(String message) {
        java.util.Map<String, String> error = new java.util.HashMap<>();
        error.put("error", message);
        return error;
    }
}
