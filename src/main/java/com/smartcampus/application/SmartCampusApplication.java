package com.smartcampus.application;

import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.resource.DiscoveryResource;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application Entry Point.
 *
 * The @ApplicationPath annotation sets the base URI for all REST resources.
 * This class registers all resources, providers (exception mappers), and filters.
 *
 * Lifecycle Note: By default, JAX-RS creates a NEW instance of each Resource class
 * for every HTTP request (request-scoped). This means resource classes are NOT
 * singletons. To safely share in-memory data (HashMaps, ArrayLists) across requests,
 * we use a centralized DataStore singleton, which is accessed statically to prevent
 * data loss and race conditions.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resources
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception Mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        classes.add(NotFoundExceptionMapper.class);

        // Filters
        classes.add(LoggingFilter.class);

        // Jackson JSON Feature
        classes.add(JacksonFeature.class);

        return classes;
    }
}
