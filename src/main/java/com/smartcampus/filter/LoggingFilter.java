package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Part 5.5 - API Request & Response Logging Filter
 *
 * Implements both ContainerRequestFilter and ContainerResponseFilter
 * to provide full observability for every API call.
 *
 * Why use JAX-RS filters instead of Logger.info() in every method?
 * - Filters implement "cross-cutting concerns" — logic that applies to ALL
 *   endpoints without modifying any individual resource method.
 * - Adding logging to each method violates DRY (Don't Repeat Yourself).
 * - Filters are centrally maintainable: change logging format in one place.
 * - They follow the Aspect-Oriented Programming (AOP) pattern, keeping
 *   resource methods focused solely on business logic.
 * - Filters can be toggled or replaced without touching any resource code.
 *
 * @Provider registers this filter automatically with the JAX-RS runtime.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Executed BEFORE the request reaches any resource method.
     * Logs the HTTP method and full request URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info(String.format("[REQUEST]  --> %s %s", method, uri));
    }

    /**
     * Executed AFTER the response is built by the resource method.
     * Logs the final HTTP status code returned to the client.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        int statusCode = responseContext.getStatus();
        LOGGER.info(String.format("[RESPONSE] <-- %s %s | Status: %d", method, uri, statusCode));
    }
}
