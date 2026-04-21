package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 2 - Room Management Resource
 *
 * Manages /api/v1/rooms
 *
 * GET    /rooms         - List all rooms
 * POST   /rooms         - Create a new room
 * GET    /rooms/{roomId} - Get room details
 * DELETE /rooms/{roomId} - Delete a room (blocked if sensors assigned)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a comprehensive list of all rooms.
     * Returns full room objects (not just IDs) for maximum client usability.
     */
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(dataStore.getRooms().values());
        return Response.ok(roomList).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with Location header.
     */
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(buildError("Room ID is required"))
                    .build();
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(buildError("Room name is required"))
                    .build();
        }
        if (dataStore.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(buildError("Room with ID '" + room.getId() + "' already exists"))
                    .build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        dataStore.addRoom(room);

        URI location = URI.create("/api/v1/rooms/" + room.getId());
        return Response.created(location)
                .entity(room)
                .build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoomById(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(buildError("Room not found with ID: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     *
     * Deletes a room. Business Logic Constraint:
     * A room CANNOT be deleted if it still has sensors assigned to it.
     * This prevents orphaned sensor data.
     *
     * Idempotency: DELETE is idempotent by HTTP spec. However, our implementation
     * returns 404 on the second call since the room no longer exists. The server
     * state remains the same (room is absent), satisfying idempotency semantics.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoomById(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(buildError("Room not found with ID: " + roomId))
                    .build();
        }

        // Business Logic: Block deletion if room has active sensors
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Room '" + roomId + "' cannot be deleted. It has " +
                room.getSensorIds().size() + " sensor(s) still assigned: " +
                room.getSensorIds()
            );
        }

        dataStore.deleteRoom(roomId);
        return Response.noContent().build(); // 204 No Content
    }

    private java.util.Map<String, String> buildError(String message) {
        java.util.Map<String, String> error = new java.util.HashMap<>();
        error.put("error", message);
        return error;
    }
}
