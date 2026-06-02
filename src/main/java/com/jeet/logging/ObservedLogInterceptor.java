package com.jeet.logging;

import io.quarkus.arc.Unremovable;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * CDI interceptor for structured observability logging.
 *
 * <p>Emits START / END / ERROR log events for methods annotated with {@link ObservedLog}.
 * Fires only when the bean is called through the CDI proxy (i.e. via {@code @Inject}).
 *
 * <p>Event schema (JSON-like, mapped to Quarkus structured logging):
 * <pre>{@code
 * { "timestamp": "...", "level": "INFO", "category": "LOG-ENTITY",
 *   "source": "DAO.getAllMovies", "event": "START", "durationMs": null }
 * }</pre>
 *
 * <p>File routing is controlled by Quarkus log handlers in {@code application.properties}.
 * Each category maps to a dedicated file — no merged output.
 *
 * <p><b>CDI proxy constraint:</b>
 * This interceptor only fires for CDI-managed calls. mbook-quarkus DAO is {@code @RequestScoped}
 * and injected via {@code @Inject} — interceptor fires correctly without changes.
 */
@ObservedLog(category = "")
@Interceptor
@Unremovable
@Priority(Interceptor.Priority.APPLICATION)
public class ObservedLogInterceptor {

    /**
     * Parameter name fragments that must always be redacted.
     * Matched case-insensitively as a substring of the runtime parameter name.
     * Requires the module compiled with {@code -parameters} (see pom.xml).
     * SEC-001/SEC-002 enforcement.
     */
    private static final Set<String> SENSITIVE_NAMES = Set.of(
            "uuid", "token", "password", "passwd", "pass", "secret",
            "key", "nonce", "pin", "card", "cvv", "credential", "auth"
    );

    private static final String REDACTED = "***";

    @AroundInvoke
    public Object around(InvocationContext ctx) throws Exception {

        ObservedLog cfg = resolveAnnotation(ctx);
        if (cfg == null) {
            return ctx.proceed();
        }

        long start = System.currentTimeMillis();
        String source = ctx.getTarget().getClass().getSimpleName()
                + "." + ctx.getMethod().getName();


        emit(cfg.level(), cfg.category(), source, "START",
                cfg.includeArgs() ? redactArgs(ctx.getMethod().getParameters(), ctx.getParameters(), cfg.maskedArgIndices()) : null, null);

        try {
            Object result = ctx.proceed();
            long durationMs = System.currentTimeMillis() - start;

            Map<String, Object> extra = cfg.includeResult() && result != null
                    ? Map.of("durationMs", durationMs, "result", result.toString())
                    : Map.of("durationMs", durationMs);

            emit(cfg.level(), cfg.category(), source, "END", null, extra);
            return result;

        } catch (Exception ex) {
            long durationMs = System.currentTimeMillis() - start;
            emit(LogLevel.ERROR, cfg.category(), source, "ERROR", null,
                    Map.of("durationMs", durationMs, "error", ex.getClass().getSimpleName()));
            throw ex;
        }
    }

    /**
     * Builds a safe copy of the parameter value array for logging.
     *
     * <p>A parameter is redacted (replaced with {@value #REDACTED}) if:
     * <ol>
     *   <li>Its zero-based index appears in {@code maskedIndices} (explicit annotation control), OR</li>
     *   <li>Its runtime name (available when compiled with {@code -parameters}) contains any fragment
     *       from {@link #SENSITIVE_NAMES} (case-insensitive) — SEC-001/SEC-002 heuristic.</li>
     * </ol>
     *
     * <p>If parameter names are not available at runtime (compiled without {@code -parameters}),
     * generic names like {@code arg0} are produced — these never match {@link #SENSITIVE_NAMES},
     * so explicit {@code maskedArgIndices} is the reliable fallback in that case.
     *
     * @param parameters   method {@link Parameter} array (provides runtime names when {@code -parameters} is set)
     * @param values       actual argument values from {@link InvocationContext#getParameters()}
     * @param maskedIndices zero-based indices declared in {@link ObservedLog#maskedArgIndices()}
     * @return a new array where sensitive positions are replaced with {@value #REDACTED}
     */
    private Object[] redactArgs(Parameter[] parameters, Object[] values, int[] maskedIndices) {
        if (values == null || values.length == 0) {
            return values;
        }
        Object[] safe = Arrays.copyOf(values, values.length);
        for (int i = 0; i < safe.length; i++) {
            if (isExplicitlyMasked(i, maskedIndices) || isSensitiveName(parameters, i)) {
                safe[i] = REDACTED;
            }
        }
        return safe;
    }

    private boolean isExplicitlyMasked(int index, int[] maskedIndices) {
        for (int masked : maskedIndices) {
            if (masked == index) return true;
        }
        return false;
    }

    private boolean isSensitiveName(Parameter[] parameters, int index) {
        if (parameters == null || index >= parameters.length) return false;
        String name = parameters[index].getName().toLowerCase();
        for (String fragment : SENSITIVE_NAMES) {
            if (name.contains(fragment)) return true;
        }
        return false;
    }

    /**
     * Method-level annotation takes precedence; falls back to type-level.
     */
    private ObservedLog resolveAnnotation(InvocationContext ctx) {
        ObservedLog cfg = ctx.getMethod().getAnnotation(ObservedLog.class);
        if (cfg != null) {
            return cfg;
        }

        // CDI proxies can expose methods without inherited annotations; resolve on target type.
        Class<?> targetClass = ctx.getTarget().getClass();
        try {
            cfg = targetClass
                    .getMethod(ctx.getMethod().getName(), ctx.getMethod().getParameterTypes())
                    .getAnnotation(ObservedLog.class);
            if (cfg != null) {
                return cfg;
            }
        } catch (NoSuchMethodException ignored) {
            // try superclass below
        }

        Class<?> superClass = targetClass.getSuperclass();
        if (superClass != null) {
            try {
                cfg = superClass
                        .getMethod(ctx.getMethod().getName(), ctx.getMethod().getParameterTypes())
                        .getAnnotation(ObservedLog.class);
                if (cfg != null) {
                    return cfg;
                }
            } catch (NoSuchMethodException ignored) {
                // fall through to type-level lookup
            }
        }

        cfg = targetClass.getAnnotation(ObservedLog.class);
        if (cfg != null) {
            return cfg;
        }
        return superClass != null ? superClass.getAnnotation(ObservedLog.class) : null;
    }

    /**
     * Emits a structured log event to the category logger.
     * The logger name matches the category so Quarkus can route it to the correct file handler.
     */
    private void emit(LogLevel level, String category, String source, String event,
                      Object[] args, Map<String, Object> extra) {
        Logger catLogger = Logger.getLogger(category);
        String msg = buildMessage(category, source, event, args, extra);
        switch (level) {
            case TRACE -> catLogger.trace(msg);
            case DEBUG -> catLogger.debug(msg);
            case WARN  -> catLogger.warn(msg);
            case ERROR -> catLogger.error(msg);
            default    -> catLogger.info(msg);
        }
    }

    private String buildMessage(String category, String source, String event,
                                Object[] args, Map<String, Object> extra) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{')
          .append("\"timestamp\":").append(toJsonValue(Instant.now().toString()))
          .append(",\"category\":").append(toJsonValue(category))
          .append(",\"source\":").append(toJsonValue(source))
          .append(",\"event\":").append(toJsonValue(event));

        if (args != null) {
            sb.append(",\"args\":").append(toJsonValue(args));
        }
        if (extra != null) {
            extra.forEach((k, v) -> sb.append(',').append(toJsonValue(k)).append(':').append(toJsonValue(v)));
        }
        sb.append('}');
        return sb.toString();
    }

    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(toJsonValue(String.valueOf(e.getKey())))
                  .append(':')
                  .append(toJsonValue(e.getValue()));
                first = false;
            }
            sb.append('}');
            return sb.toString();
        }
        if (value instanceof Object[] array) {
            return toJsonValue(Arrays.asList(array));
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(toJsonValue(item));
                first = false;
            }
            sb.append(']');
            return sb.toString();
        }
        return '"' + escapeJson(String.valueOf(value)) + '"';
    }

    private String escapeJson(String s) {
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}

