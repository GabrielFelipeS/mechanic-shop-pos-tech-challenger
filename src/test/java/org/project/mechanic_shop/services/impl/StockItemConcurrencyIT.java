package org.project.mechanic_shop.services.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.project.mechanic_shop.application.ports.StockItemRepositoryPort;
import org.project.mechanic_shop.application.services.StockItemService;
import org.project.mechanic_shop.config.AbstractIntegrationTest;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.project.mechanic_shop.domain.enums.StockItemTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

class StockItemConcurrencyIT extends AbstractIntegrationTest {

	@Autowired
	private StockItemService stockItemService;

	@Autowired
	private StockItemRepositoryPort stockItemRepository;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void shouldNotLoseUpdatesWhenWithdrawingStockConcurrently() throws Exception {
		StockItem saved = stockItemRepository.save(buildStockItem("CONC-01", "Peca Concorrente", 6));

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch bothReady = new CountDownLatch(2);
		CountDownLatch go = new CountDownLatch(1);

		Runnable withdrawFive = () -> {
			bothReady.countDown();
			try {
				go.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			stockItemService.withdrawStock(saved.getExternalId(), 5);
		};

		try {
			List<Future<?>> futures = List.of(
				executor.submit(withdrawFive),
				executor.submit(withdrawFive)
			);

			assertThat(bothReady.await(5, TimeUnit.SECONDS)).isTrue();
			go.countDown();

			for (Future<?> future : futures) {
				future.get(10, TimeUnit.SECONDS);
			}
		} finally {
			executor.shutdownNow();
		}

		StockItem updated = stockItemRepository.findByExternalId(saved.getExternalId()).orElseThrow();

		assertThat(updated.getQuantity()).isZero();
		assertThat(updated.getPendingDemand()).isEqualTo(4);
	}

	private StockItem buildStockItem(String code, String name, int quantity) {
		StockItem item = new StockItem();
		item.setCode(code);
		item.setName(name);
		item.setType(StockItemTypeEnum.PART);
		item.setDescription("Item de estoque para teste de concorrencia");
		item.setQuantity(quantity);
		item.setPendingDemand(0);
		item.setCostPrice(new BigDecimal("20.00"));
		item.setSalePrice(new BigDecimal("35.00"));
		return item;
	}
}
