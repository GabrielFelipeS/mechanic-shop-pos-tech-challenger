package org.project.mechanic_shop.dto.stock_item_dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public record StockWithdrawalDto(
	@NotNull(message = "A quantidade é obrigatória.")
	@Min(value = 1, message = "A quantidade a ser retirada deve ser de pelo menos 1.")
	Integer quantity
) implements Serializable {}
