package org.project.mechanic_shop.shared.config.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.shared.config.observability.ObservabilityReporter;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@Slf4j
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

	private final ObservabilityReporter observabilityReporter;

	/**
	 * Asynchronous service order side effects (notifications, telemetry) fail silently by design —
	 * nothing is waiting on their result. Reporting them to New Relic here is what makes the
	 * "service order processing failures" alert possible.
	 */
	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return (throwable, method, params) -> {
			log.error(
				"Uncaught exception in async method '{}': {}",
				method.getName(),
				throwable.getMessage(),
				throwable
			);
			observabilityReporter.recordServiceOrderFailure(method.getName(), throwable);
		};
	}
}
