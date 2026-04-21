package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Part 5.4 - Global Safety Net: Catch-All ExceptionMapper
 *
 * Intercepts ANY unexpected runtime error (NullPointerException,
 * IndexOutOfBoundsException, etc.) and returns a clean HTTP 500
 * Internal Server Error with a generic JSON body.
 *
 * CRITICAL SECURITY DESIGN:
 * This mapper ensures that raw Java stack traces are NEVER exposed to
 * API consumers. Stack traces are a serious cybersecurity risk because
 * they can reveal:
 *   - Internal package/class structure (attack surface mapping)
 *   - Library names and versions (known CVE exploitation)
 *   - File system paths (directory traversal hints)
 *   - Business logic flow (logic flaw exploitation)
 *   - Database query fragments (SQL injection hints)
 *
 * The stack trace is logged server-side only, where it is safe to inspect.
 * The client receives only a generic, non-revealing error message.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace SERVER-SIDE for debugging (never sent to client)
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global safety net: "
                + exception.getMessage(), exception);

        // Return a safe, generic error to the client
        ErrorResponse error = new ErrorResponse(
            500,
            "Internal Server Error",
            "An unexpected error occurred. Please contact the system administrator."
        );

        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }
}
