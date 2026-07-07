package org.project.mechanic_shop.domain.dto.stock_item_dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.project.mechanic_shop.domain.enums.StockItemTypeEnum;

import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "Payload for creating or updating a stock item")
public record StockItemManDto(
	@Schema(description = "Internal item code", example = "BRK-100")
	@NotBlank(message = "Code is required.")
	@Size(max = 50, message = "Code must not exceed 50 characters.")
	String code,

	@Schema(description = "Item name", example = "Pastilha de freio dianteira")
	@NotBlank(message = "Name is required.")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String name,

	@Schema(description = "Item type: PART or CONSUMABLE", example = "PART")
	@NotNull(message = "Type is required (PART or CONSUMABLE).") StockItemTypeEnum type,

	@Schema(description = "Optional description", example = "Compatível com veículos Volkswagen")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.") String description,

	@Schema(description = "Current stock quantity", example = "50")
	@NotNull(message = "Quantity is required.")
	@Min(value = 0, message = "Quantity cannot be negative.")
	Integer quantity,

	@Schema(description = "Purchase cost price", example = "35.00")
	@NotNull(message = "Cost price is required.")
	@DecimalMin(value = "0.0", inclusive = true, message = "Cost price cannot be negative.")
	BigDecimal costPrice,

	@Schema(description = "Sale price charged to the customer", example = "75.00")
	@NotNull(message = "Sale price is required.")
	@DecimalMin(value = "0.0", inclusive = true, message = "Sale price cannot be negative.")
	BigDecimal salePrice
) implements Serializable {}
