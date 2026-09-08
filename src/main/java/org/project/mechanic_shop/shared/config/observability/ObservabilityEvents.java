package org.project.mechanic_shop.shared.config.observability;

/**
 * Names of the custom New Relic events emitted by the application. The dashboards and alert
 * conditions in {@code infra/newrelic/terraform} query these event types by name, so renaming
 * anything here means updating the NRQL there as well.
 */
public final class ObservabilityEvents {

	/** One event per service order created — feeds the daily service order volume widget. */
	public static final String SERVICE_ORDER_OPENED = "MechanicShopServiceOrderOpened";

	/** One event per status transition — feeds the average time per phase widgets. */
	public static final String SERVICE_ORDER_STATUS_CHANGED = "MechanicShopServiceOrderStatusChanged";

	/** One event per failure while processing a service order — feeds the alert conditions. */
	public static final String SERVICE_ORDER_FAILURE = "MechanicShopServiceOrderFailure";

	/** One event per outbound integration failure (SMTP, HTTP, …) — feeds the integrations widget. */
	public static final String INTEGRATION_FAILURE = "MechanicShopIntegrationFailure";

	/** Business phases reported by {@link #SERVICE_ORDER_STATUS_CHANGED}. */
	public static final String PHASE_DIAGNOSIS = "DIAGNOSIS";
	public static final String PHASE_EXECUTION = "EXECUTION";
	public static final String PHASE_FINALIZATION = "FINALIZATION";

	private ObservabilityEvents() {}
}
