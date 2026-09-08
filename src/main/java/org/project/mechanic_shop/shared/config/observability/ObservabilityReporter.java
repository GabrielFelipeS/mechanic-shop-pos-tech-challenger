package org.project.mechanic_shop.shared.config.observability;

import com.newrelic.api.agent.NewRelic;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Single entry point for reporting failures to New Relic.
 *
 * <p>Reporting telemetry must never change the outcome of the caller, so both methods swallow their
 * own exceptions and only log a warning when the agent is unavailable.
 */
@Component
@Slf4j
public class ObservabilityReporter {

	/**
	 * Reports a failure while processing a service order. Feeds the "service order processing
	 * failures" alert condition.
	 */
	public void recordServiceOrderFailure(String operation, Throwable throwable) {
		Map<String, Object> attributes = baseAttributes();
		attributes.put("operation", operation);
		attributes.put("errorClass", throwable.getClass().getName());
		attributes.put("errorMessage", String.valueOf(throwable.getMessage()));

		record(ObservabilityEvents.SERVICE_ORDER_FAILURE, attributes, throwable);
	}

	/**
	 * Reports a failure calling an external dependency (SMTP, HTTP, …). Feeds the "integration
	 * errors" dashboard widget.
	 */
	public void recordIntegrationFailure(String integration, String operation, Throwable throwable) {
		Map<String, Object> attributes = baseAttributes();
		attributes.put("integration", integration);
		attributes.put("operation", operation);
		attributes.put("errorClass", throwable.getClass().getName());
		attributes.put("errorMessage", String.valueOf(throwable.getMessage()));

		record(ObservabilityEvents.INTEGRATION_FAILURE, attributes, throwable);
	}

	private void record(String eventType, Map<String, Object> attributes, Throwable throwable) {
		try {
			NewRelic.getAgent().getInsights().recordCustomEvent(eventType, attributes);
			NewRelic.noticeError(throwable, attributes);
		} catch (Exception e) {
			log.warn("Failed to report '{}' telemetry: {}", eventType, e.getMessage());
		}
	}

	private Map<String, Object> baseAttributes() {
		Map<String, Object> attributes = new HashMap<>();
		String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
		if (correlationId != null) attributes.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, correlationId);
		return attributes;
	}
}
