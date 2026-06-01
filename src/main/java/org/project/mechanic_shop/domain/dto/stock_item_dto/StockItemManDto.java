package org.project.mechanic_shop.domain.dto.stock_item_dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;
import org.project.mechanic_shop.domain.enums.StockItemTypeEnum;

/**
 * DTO for {@link StockItem}
 */
public record StockItemManDto(
	@NotBlank(message = "Code is required.")
	@Size(max = 50, message = "Code must not exceed 50 characters.")
	String code,

	@NotBlank(message = "Name is required.")
	@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.")
	String name,

	@NotNull(message = "Type is required (PART or CONSUMABLE).") StockItemTypeEnum type,

	@Pattern(regexp = "^[^<>]*$", message = "HTML tags are not allowed.") String description,

	@NotNull(message = "Quantity is required.")
	@Min(value = 0, message = "Quantity cannot be negative.")
	Integer quantity,

	@NotNull(message = "Cost price is required.")
	@DecimalMin(value = "0.0", inclusive = true, message = "Cost price cannot be negative.")
	BigDecimal costPrice,

	@NotNull(message = "Sale price is required.")
	@DecimalMin(value = "0.0", inclusive = true, message = "Sale price cannot be negative.")
	BigDecimal salePrice
) implements Serializable {}
