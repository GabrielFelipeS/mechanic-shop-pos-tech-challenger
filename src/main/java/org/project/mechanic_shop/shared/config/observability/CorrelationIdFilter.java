package org.project.mechanic_shop.shared.config.observability;

import com.newrelic.api.agent.NewRelic;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Propagates a correlation id across every request so that structured logs, APM transactions and
 * distributed traces can be joined together.
 *
 * <p>The id is taken from the inbound {@code X-Correlation-Id} (or {@code X-Request-Id}) header when
 * a caller — API Gateway, Lambda authorizer, another service — already provides one, otherwise a new
 * one is generated. It is published in three places:
 *
 * <ul>
 *   <li>the SLF4J {@link MDC}, so it shows up in every structured (JSON) log line;
 *   <li>the New Relic transaction, as a custom attribute, so logs and APM can be cross-filtered;
 *   <li>the response header, so clients and the gateway can report it back on failures.
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";
	public static final String CORRELATION_ID_MDC_KEY = "correlationId";

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		var correlationId = resolveCorrelationId(request);

		MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
		NewRelic.addCustomParameter(CORRELATION_ID_MDC_KEY, correlationId);
		response.setHeader(CORRELATION_ID_HEADER, correlationId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(CORRELATION_ID_MDC_KEY);
		}
	}

	private String resolveCorrelationId(HttpServletRequest request) {
		var inbound = firstNonBlank(request.getHeader(CORRELATION_ID_HEADER), request.getHeader(REQUEST_ID_HEADER));
		return inbound != null ? inbound : UUID.randomUUID().toString();
	}

	private String firstNonBlank(String first, String second) {
		if (first != null && !first.isBlank()) return first;
		if (second != null && !second.isBlank()) return second;
		return null;
	}
}
