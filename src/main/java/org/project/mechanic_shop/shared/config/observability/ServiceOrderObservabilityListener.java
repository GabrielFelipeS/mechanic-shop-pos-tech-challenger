package org.project.mechanic_shop.shared.config.observability;

import com.newrelic.api.agent.NewRelic;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;
import org.project.mechanic_shop.domain.events.NewServiceOrderEvent;
import org.project.mechanic_shop.domain.events.ServiceOrderStatusChangedEvent;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Turns service order domain events into New Relic custom events and structured log records.
 *
 * <p>Kept separate from {@code ServiceOrderNotificationListener} on purpose: notifications are
 * business behaviour, this is telemetry, and a failure to report telemetry must never break the
 * business flow — hence every method here swallows its own exceptions.
 *
 * <p>Phase durations are derived from the timestamps the domain already persists
 * ({@code createdAt}, {@code approvalDate}, {@code actualCompletionDate}), so no extra table or
 * in-memory state is required and the numbers survive a pod restart or a scale-out.
 */
@Component
@Slf4j
public class ServiceOrderObservabilityListener {

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onServiceOrderOpened(NewServiceOrderEvent event) {
		ServiceOrder order = event.serviceOrder();
		try {
			Map<String, Object> attributes = baseAttributes(order);
			attributes.put("status", order.getStatus() != null ? order.getStatus().name() : "UNKNOWN");
			NewRelic.getAgent().getInsights().recordCustomEvent(ObservabilityEvents.SERVICE_ORDER_OPENED, attributes);

			log.info(
				"service_order.opened serviceOrderId={} status={}",
				order.getExternalId(),
				order.getStatus()
			);
		} catch (Exception e) {
			log.warn("Failed to report service order opened telemetry: {}", e.getMessage());
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onStatusChanged(ServiceOrderStatusChangedEvent event) {
		ServiceOrder order = event.serviceOrder();
		try {
			Map<String, Object> attributes = baseAttributes(order);
			attributes.put("oldStatus", nameOf(event.oldStatus()));
			attributes.put("newStatus", nameOf(event.newStatus()));

			String phase = phaseFor(event.newStatus());
			Long phaseDurationSeconds = phaseDurationSeconds(order, event.newStatus());

			if (phase != null) attributes.put("phase", phase);
			if (phaseDurationSeconds != null) attributes.put("phaseDurationSeconds", phaseDurationSeconds);

			NewRelic
				.getAgent()
				.getInsights()
				.recordCustomEvent(ObservabilityEvents.SERVICE_ORDER_STATUS_CHANGED, attributes);

			log.info(
				"service_order.status_changed serviceOrderId={} oldStatus={} newStatus={} phase={} phaseDurationSeconds={}",
				order.getExternalId(),
				nameOf(event.oldStatus()),
				nameOf(event.newStatus()),
				phase,
				phaseDurationSeconds
			);
		} catch (Exception e) {
			log.warn("Failed to report service order status telemetry: {}", e.getMessage());
		}
	}

	/**
	 * Maps a reached status to the business phase whose duration just became measurable.
	 *
	 * <p>{@code IN_PROGRESS} closes the diagnosis/approval phase, {@code COMPLETED} closes the
	 * execution phase and {@code DELIVERED} closes the hand-over phase. Every other transition is
	 * still recorded, just without a phase duration.
	 */
	private String phaseFor(ServiceOrderStatusEnum newStatus) {
		if (newStatus == null) return null;
		return switch (newStatus) {
			case IN_PROGRESS -> ObservabilityEvents.PHASE_DIAGNOSIS;
			case COMPLETED -> ObservabilityEvents.PHASE_EXECUTION;
			case DELIVERED -> ObservabilityEvents.PHASE_FINALIZATION;
			default -> null;
		};
	}

	private Long phaseDurationSeconds(ServiceOrder order, ServiceOrderStatusEnum newStatus) {
		if (newStatus == null) return null;
		return switch (newStatus) {
			case IN_PROGRESS -> secondsBetween(order.getCreatedAt(), order.getApprovalDate());
			case COMPLETED -> secondsBetween(order.getApprovalDate(), order.getActualCompletionDate());
			case DELIVERED -> secondsBetween(order.getActualCompletionDate(), now());
			default -> null;
		};
	}

	private Map<String, Object> baseAttributes(ServiceOrder order) {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("serviceOrderId", String.valueOf(order.getExternalId()));

		Long openForSeconds = secondsBetween(order.getCreatedAt(), now());
		if (openForSeconds != null) attributes.put("openForSeconds", openForSeconds);

		String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
		if (correlationId != null) attributes.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, correlationId);

		return attributes;
	}

	private Long secondsBetween(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) return null;
		long seconds = Duration.between(start, end).toSeconds();
		return seconds >= 0 ? seconds : null;
	}

	private LocalDateTime now() {
		return LocalDateTime.now(ZoneId.systemDefault());
	}

	private String nameOf(ServiceOrderStatusEnum status) {
		return status != null ? status.name() : "UNKNOWN";
	}
}
