package com.jeet.logging;

import jakarta.interceptor.InterceptorBinding;
import jakarta.enterprise.util.Nonbinding;
import java.lang.annotation.*;

/**
 * Marks a CDI bean method (or type) for structured logging via {@link ObservedLogInterceptor}.
 *
 * <p>Usage (method-level preferred per spec):
 * <pre>{@code
 * @ObservedLog(category = "LOG-ENTITY", level = LogLevel.INFO)
 * public List<Movie> getAllMovies(...) { ... }
 * }</pre>
 *
 * <p>Category values follow the routing map in speckit.system.backend-logging-observability.implementation.md:
 * <ul>
 *   <li>{@code LOG-ENTITY}        → entity.log</li>
 *   <li>{@code LOG-HIBERNATE-TX}  → hibernate-tx.log</li>
 *   <li>{@code LOG-ENDPOINT}      → endpoint.log</li>
 *   <li>{@code LOG-FILTER}        → filter.log</li>
 *   <li>{@code LOG-SESSION}       → session.log</li>
 * </ul>
 *
 * <p><b>Security note:</b> {@code includeArgs} defaults to {@code false}.
 * Enable only on non-sensitive methods; sensitive params (uuid, tokens, payment) must NOT be logged.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@InterceptorBinding
public @interface ObservedLog {

    /**
     * Log category — drives file routing. Must match one of the known category IDs.
     * See category→file map in the implementation spec.
     */
    @Nonbinding
    String category();

    /**
     * Minimum log level for this observation point. Default: INFO.
     */
    @Nonbinding
    LogLevel level() default LogLevel.INFO;

    /**
     * If true, method arguments are appended to the START event as "args".
     * Disabled by default for security-by-default posture.
     * Never enable for methods that receive uuid, token, password, or payment fields.
     */
    @Nonbinding
    boolean includeArgs() default false;

    /**
     * If true, method return value is appended to the END event as "result".
     * Disabled by default. Use only for non-sensitive scalar results.
     */
    @Nonbinding
    boolean includeResult() default false;

    /**
     * Zero-based parameter indices that must always be masked regardless of name heuristics.
     * Use this to explicitly protect sensitive positional arguments (uuid, token, password, etc.)
     * when {@code includeArgs = true}.
     * Example: {@code maskedArgIndices = {0, 2}} masks the 1st and 3rd parameter.
     */
    @Nonbinding
    int[] maskedArgIndices() default {};
}

