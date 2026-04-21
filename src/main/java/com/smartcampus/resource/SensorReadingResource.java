package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Part 4.2 - Historical Data Management (Sub-Resource)
 *
 * This class handles /api/v1/sensors/{sensorId}/readings
 * It is NOT registered directly — it is returned by SensorResource's
 * sub-resource locator method, following the Sub-Resource Locator Pattern.
 *
 * Benefits: Each class has a single responsibility. Complex APIs with
 * many nested paths remain manageable without a monolithic controller.
 *
 * GET  /  - Retrieve all historical readings for the sensor
 * POST /  - Append a new reading (also updates parent sensor's currentValue)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Retrieves the full historical log of readings for this sensor.
     */
    @GET
    public Response getReadings() {
        List<SensorReading> readings = dataStore.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     *
     * Appends a new sensor reading to the historical log.
     *
     * Business Constraint: A sensor in "MAINTENANCE" status is physically
     * disconnected and cannot accept new readings.
     * Throws SensorUnavailableException → mapped to HTTP 403 Forbidden.
     *
     * Side Effect: Updates the parent Sensor's currentValue field
     * to keep data consistent across the API.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensorById(sensorId);

        // State constraint: MAINTENANCE sensors cannot receive readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently under MAINTENANCE " +
                "and cannot accept new readings."
            );
        }

        // Also block OFFLINE sensors
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is OFFLINE and cannot accept new readings."
            );
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(buildError("Reading body is required"))
                    .build();
        }

        // Auto-generate ID if not provided
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Auto-set timestamp if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist reading + update parent sensor's currentValue (side effect)
        dataStore.addReading(sensorId, reading);

        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location)
                .entity(reading)
                .build();
    }

    private java.util.Map<String, String> buildError(String message) {
        java.util.Map<String, String> error = new java.util.HashMap<>();
        error.put("error", message);
        return error;
    }
}
