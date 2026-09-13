package org.project.mechanic_shop.shared.config.observability;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

	private final CorrelationIdFilter filter = new CorrelationIdFilter();

	@AfterEach
	void clearMdc() {
		MDC.clear();
	}

	@Test
	void shouldReuseInboundCorrelationIdHeader() throws Exception {
		var request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "abc-123");
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, capturingChain());

		assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("abc-123");
		assertThat(capturedCorrelationId).isEqualTo("abc-123");
	}

	@Test
	void shouldFallBackToRequestIdHeader() throws Exception {
		var request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.REQUEST_ID_HEADER, "gateway-999");
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, capturingChain());

		assertThat(capturedCorrelationId).isEqualTo("gateway-999");
	}

	@Test
	void shouldGenerateCorrelationIdWhenNoHeaderIsPresent() throws Exception {
		var response = new MockHttpServletResponse();

		filter.doFilter(new MockHttpServletRequest(), response, capturingChain());

		assertThat(capturedCorrelationId).isNotBlank();
		assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(capturedCorrelationId);
	}

	@Test
	void shouldIgnoreBlankInboundHeader() throws Exception {
		var request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ");
		var response = new MockHttpServletResponse();

		filter.doFilter(request, response, capturingChain());

		assertThat(capturedCorrelationId).isNotBlank().isNotEqualTo("   ");
	}

	@Test
	void shouldRemoveCorrelationIdFromMdcAfterTheRequest() throws Exception {
		filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), capturingChain());

		assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
	}

	@Test
	void shouldClearMdcEvenWhenTheChainThrows() {
		FilterChain failingChain = (req, res) -> {
			throw new IllegalStateException("boom");
		};

		try {
			filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), failingChain);
		} catch (Exception ignored) {
			// the filter must not swallow the failure, only clean up after itself
		}

		assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
	}

	private String capturedCorrelationId;

	/** Captures the MDC value as seen by the rest of the chain, i.e. while the request is running. */
	private FilterChain capturingChain() {
		return (req, res) -> capturedCorrelationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
	}
}
