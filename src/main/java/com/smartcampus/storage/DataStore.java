package com.smartcampus.storage;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized in-memory DataStore (Singleton).
 *
 * Because JAX-RS creates a new Resource instance per request, shared data
 * must be stored here using thread-safe ConcurrentHashMap collections.
 * This prevents race conditions and data loss across concurrent requests.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    // Thread-safe maps for in-memory persistence
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        // Seed with sample data
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ---- Room Operations ----

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // ---- Sensor Operations ----

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Add sensorId to the parent room's sensorIds list
        Room room = rooms.get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().add(sensor.getId());
        }
        // Initialize readings list for this sensor
        sensorReadings.putIfAbsent(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    // ---- SensorReading Operations ----

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, Collections.synchronizedList(new ArrayList<>()));
    }

    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()))
                      .add(reading);
        // Update parent sensor's currentValue as a side effect
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }

    // ---- Seed Sample Data ----

    private void seedData() {
        // Seed Rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        Room r3 = new Room("HALL-A", "Main Hall", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Seed Sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "HALL-A");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Link sensors to rooms
        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());
        r3.getSensorIds().add(s3.getId());

        // Initialize readings lists
        sensorReadings.put(s1.getId(), Collections.synchronizedList(new ArrayList<>()));
        sensorReadings.put(s2.getId(), Collections.synchronizedList(new ArrayList<>()));
        sensorReadings.put(s3.getId(), Collections.synchronizedList(new ArrayList<>()));

        // Seed sample readings
        SensorReading sr1 = new SensorReading(UUID.randomUUID().toString(),
                System.currentTimeMillis() - 60000, 21.8);
        SensorReading sr2 = new SensorReading(UUID.randomUUID().toString(),
                System.currentTimeMillis(), 22.5);
        sensorReadings.get(s1.getId()).add(sr1);
        sensorReadings.get(s1.getId()).add(sr2);
    }
}
