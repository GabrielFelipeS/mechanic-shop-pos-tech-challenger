package org.project.mechanic_shop.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.project.mechanic_shop.config.ObjectMapperConfig;
import org.project.mechanic_shop.config.SecurityConfig;
import org.project.mechanic_shop.dto.stock_item_dto.StockItemManDto;
import org.project.mechanic_shop.dto.stock_item_dto.StockWithdrawalDto;
import org.project.mechanic_shop.mappers.StockItemMapperImpl;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;
import org.project.mechanic_shop.services.StockItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StockItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ SecurityConfig.class, StockItemMapperImpl.class, ObjectMapperConfig.class })
class StockItemControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private StockItemService service;

	@Nested
	class FindById {

		@Test
		void shouldFindStockItemByExternalId() throws Exception {
			var externalId = UUID.randomUUID();
			var stockItem = buildStockItem();

			when(service.findByExternalId(externalId)).thenReturn(stockItem);

			mockMvc
				.perform(get("/api/stock-items/{id}", externalId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.code").value(stockItem.getCode()))
				.andExpect(jsonPath("$.data.name").value(stockItem.getName()));
		}

		@Test
		void shouldReturnNotFoundWhenStockItemDoesNotExist() throws Exception {
			var externalId = UUID.randomUUID();

			when(service.findByExternalId(externalId)).thenThrow(EntityNotFoundException.class);

			mockMvc.perform(get("/api/stock-items/{id}", externalId)).andExpect(status().isNotFound());
		}
	}

	@Nested
	class Create {

		@Test
		void shouldCreateStockItem() throws Exception {
			var stockItem = buildStockItem();
			var dto = buildStockItemManDto();

			when(service.create(any(StockItem.class))).thenReturn(stockItem);

			mockMvc
				.perform(
					post("/api/stock-items/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data").value(stockItem.getExternalId().toString()));
		}

		@Test
		void shouldReturnConflictWhenCreateFails() throws Exception {
			var dto = buildStockItemManDto();

			when(service.create(any(StockItem.class))).thenThrow(new IllegalArgumentException("duplicate"));

			mockMvc
				.perform(
					post("/api/stock-items/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
		}
	}

	@Nested
	class Update {

		@Test
		void shouldUpdateStockItem() throws Exception {
			var externalId = UUID.randomUUID();
			var stockItem = buildStockItem();
			var dto = buildStockItemManDto();

			when(service.update(eq(externalId), any(StockItem.class))).thenReturn(stockItem);

			mockMvc
				.perform(
					put("/api/stock-items/{id}", externalId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.externalId").value(stockItem.getExternalId().toString()))
				.andExpect(jsonPath("$.data.name").value(stockItem.getName()));

			verify(service).update(eq(externalId), any(StockItem.class));
		}

		@Test
		void shouldReturnConflictWhenUpdateFails() throws Exception {
			var externalId = UUID.randomUUID();
			var dto = buildStockItemManDto();

			when(service.update(eq(externalId), any(StockItem.class))).thenThrow(
				new IllegalArgumentException("duplicate")
			);

			mockMvc
				.perform(
					put("/api/stock-items/{id}", externalId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
		}
	}

	@Nested
	class Search {

		@Test
		void shouldSearchWithPageable() throws Exception {
			var stockItem = buildStockItem();
			ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
			Page<StockItem> page = new PageImpl<>(List.of(stockItem), PageRequest.of(0, 2), 1);

			when(service.search(eq(stockItem.getCode()), eq(stockItem.getName()), any(Pageable.class))).thenReturn(
				page
			);

			mockMvc
				.perform(
					get("/api/stock-items/search")
						.param("code", stockItem.getCode())
						.param("name", stockItem.getName())
						.param("page", "0")
						.param("size", "10")
						.param("sort", "name,asc")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.content[0].code").value(stockItem.getCode()))
				.andExpect(jsonPath("$.data.content[0].name").value(stockItem.getName()));

			verify(service).search(eq(stockItem.getCode()), eq(stockItem.getName()), captor.capture());

			Pageable pageable = captor.getValue();
			assertThat(pageable.getPageNumber()).isZero();
			assertThat(pageable.getPageSize()).isEqualTo(10);
			assertThat(pageable.getSort().getOrderFor("name")).isNotNull();
			assertThat(pageable.getSort().getOrderFor("name").isAscending()).isTrue();
		}

		@Test
		void shouldSearchWithDefaultPageable() throws Exception {
			var stockItem = buildStockItem();
			ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
			Page<StockItem> page = new PageImpl<>(List.of(stockItem), PageRequest.of(0, 2), 1);

			when(service.search(eq(stockItem.getCode()), any(), any(Pageable.class))).thenReturn(page);

			mockMvc
				.perform(get("/api/stock-items/search").param("code", stockItem.getCode()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.content[0].code").value(stockItem.getCode()));

			verify(service).search(eq(stockItem.getCode()), eq(null), captor.capture());

			Pageable pageable = captor.getValue();
			assertThat(pageable.getPageNumber()).isZero();
			assertThat(pageable.getPageSize()).isEqualTo(10);
			assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
			assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
		}
	}

	@Nested
	class Withdraw {

		@Test
		void shouldWithdrawStock() throws Exception {
			var externalId = UUID.randomUUID();
			var dto = new StockWithdrawalDto(3);

			mockMvc
				.perform(
					patch("/api/stock-items/{id}/withdraw", externalId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("Inventory updated successfully."));

			verify(service).withdrawStock(externalId, dto.quantity());
		}
	}

	private StockItem buildStockItem() {
		StockItem stockItem = new StockItem();
		stockItem.setId(1L);
		stockItem.setExternalId(UUID.randomUUID());
		stockItem.setCode("P-100");
		stockItem.setName("Filtro de oleo");
		stockItem.setType(StockItemTypeEnum.PART);
		stockItem.setDescription("Filtro");
		stockItem.setQuantity(10);
		stockItem.setPendingDemand(0);
		stockItem.setCostPrice(new BigDecimal("12.00"));
		stockItem.setSalePrice(new BigDecimal("20.00"));
		return stockItem;
	}

	private StockItemManDto buildStockItemManDto() {
		return new StockItemManDto(
			"P-100",
			"Filtro de oleo",
			StockItemTypeEnum.PART,
			"Filtro",
			10,
			new BigDecimal("12.00"),
			new BigDecimal("20.00")
		);
	}
}
