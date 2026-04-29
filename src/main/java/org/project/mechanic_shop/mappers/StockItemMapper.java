package org.project.mechanic_shop.mappers;

import org.mapstruct.Mapper;
import org.project.mechanic_shop.dto.stock_item_dto.*;
import org.project.mechanic_shop.models.StockItem;

@Mapper(componentModel = "spring")
public interface StockItemMapper {
	StockItem toEntity(StockItemManDto stockItemManDto);

	StockItemShortDto toShortDto(StockItem stockItem);

	StockItemDto toDto(StockItem stockItem);
}
