package org.project.mechanic_shop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.project.mechanic_shop.dto.stock_item_dto.StockItemManDto;
import org.project.mechanic_shop.dto.stock_item_dto.StockWithdrawalDto;
import org.project.mechanic_shop.models.StockItem;
import org.project.mechanic_shop.models.enums.StockItemTypeEnum;
import org.project.mechanic_shop.repositories.StockItemRepository;
import org.project.mechanic_shop.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StockItemControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private StockItemRepository stockItemRepository;

    @Test
    void shouldCreateStockItemAndPersistIt() throws Exception {
        StockItemManDto payload = new StockItemManDto(
                "P-INT-01",
                "Pastilha de Freio",
                StockItemTypeEnum.PART,
                "Jogo dianteiro",
                8,
                new BigDecimal("90.00"),
                new BigDecimal("140.00")
        );

        String responseBody = mockMvc.perform(
                        post("/api/stock-items/create")
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID externalId = UUID.fromString(objectMapper.readTree(responseBody).get("data").asText());
        StockItem persistedItem = stockItemRepository.findByExternalId(externalId).orElseThrow();

        assertThat(persistedItem.getCode()).isEqualTo(payload.code());
        assertThat(persistedItem.getName()).isEqualTo(payload.name());
        assertThat(persistedItem.getType()).isEqualTo(payload.type());
        assertThat(persistedItem.getQuantity()).isEqualTo(payload.quantity());
        assertThat(persistedItem.getCostPrice()).isEqualByComparingTo(payload.costPrice());
        assertThat(persistedItem.getSalePrice()).isEqualByComparingTo(payload.salePrice());
    }

    @Test
    void shouldReturnValidationErrorWhenCreatePayloadIsInvalid() throws Exception {
        StockItemManDto invalidPayload = new StockItemManDto(
                "",
                "",
                null,
                "<b>descricao</b>",
                -1,
                null,
                null
        );

        mockMvc.perform(
                        post("/api/stock-items/create")
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidPayload))
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_CONTENT.value()))
                .andExpect(jsonPath("$.message").value("Validation error."))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldFindStockItemByExternalIdUsingRealRepository() throws Exception {
        StockItem savedItem = stockItemRepository.save(buildStockItem("P-INT-02", "Filtro de Ar", 5));

        mockMvc.perform(
                        get("/api/stock-items/{id}", savedItem.getExternalId())
                                .with(AuthUtil.admin())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.externalId").value(savedItem.getExternalId().toString()))
                .andExpect(jsonPath("$.data.code").value(savedItem.getCode()))
                .andExpect(jsonPath("$.data.name").value(savedItem.getName()));
    }

    @Test
    void shouldUpdateStockItemUsingRealServiceAndRepository() throws Exception {
        StockItem savedItem = stockItemRepository.save(buildStockItem("P-INT-03", "Filtro de Oleo", 10));

        StockItemManDto updatePayload = new StockItemManDto(
                "P-INT-03-UPD",
                "Filtro de Oleo Premium",
                StockItemTypeEnum.PART,
                "Atualizado",
                14,
                new BigDecimal("25.00"),
                new BigDecimal("40.00")
        );

        mockMvc.perform(
                        put("/api/stock-items/{id}", savedItem.getExternalId())
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePayload))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.externalId").value(savedItem.getExternalId().toString()))
                .andExpect(jsonPath("$.data.code").value(updatePayload.code()))
                .andExpect(jsonPath("$.data.name").value(updatePayload.name()))
                .andExpect(jsonPath("$.data.quantity").value(updatePayload.quantity()));

        StockItem updatedItem = stockItemRepository.findByExternalId(savedItem.getExternalId()).orElseThrow();

        assertThat(updatedItem.getCode()).isEqualTo(updatePayload.code());
        assertThat(updatedItem.getName()).isEqualTo(updatePayload.name());
        assertThat(updatedItem.getQuantity()).isEqualTo(updatePayload.quantity());
        assertThat(updatedItem.getCostPrice()).isEqualByComparingTo(updatePayload.costPrice());
        assertThat(updatedItem.getSalePrice()).isEqualByComparingTo(updatePayload.salePrice());
    }

    @Test
    void shouldSearchStockItemsUsingPersistedData() throws Exception {
        StockItem savedItem = stockItemRepository.save(buildStockItem("P-INT-04", "Amortecedor", 3));

        mockMvc.perform(
                        get("/api/stock-items/search")
                                .with(AuthUtil.admin())
                                .param("code", savedItem.getCode())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.content[0].externalId").value(savedItem.getExternalId().toString()))
                .andExpect(jsonPath("$.data.content[0].code").value(savedItem.getCode()))
                .andExpect(jsonPath("$.data.content[0].name").value(savedItem.getName()));
    }

    @Test
    void shouldWithdrawStockAndTrackPendingDemand() throws Exception {
        StockItem savedItem = stockItemRepository.save(buildStockItem("P-INT-05", "Lampada", 2));

        mockMvc.perform(
                        patch("/api/stock-items/{id}/withdraw", savedItem.getExternalId())
                                .with(AuthUtil.admin())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new StockWithdrawalDto(5)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Inventory updated successfully."));

        StockItem updatedItem = stockItemRepository.findByExternalId(savedItem.getExternalId()).orElseThrow();

        assertThat(updatedItem.getQuantity()).isZero();
        assertThat(updatedItem.getPendingDemand()).isEqualTo(3);
    }

    private StockItem buildStockItem(String code, String name, int quantity) {
        StockItem item = new StockItem();
        item.setCode(code);
        item.setName(name);
        item.setType(StockItemTypeEnum.PART);
        item.setDescription("Item de estoque");
        item.setQuantity(quantity);
        item.setPendingDemand(0);
        item.setCostPrice(new BigDecimal("20.00"));
        item.setSalePrice(new BigDecimal("35.00"));
        return item;
    }
}
