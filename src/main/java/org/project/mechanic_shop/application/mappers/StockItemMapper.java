package org.project.mechanic_shop.application.mappers;

import org.mapstruct.Mapper;
import org.project.mechanic_shop.domain.dto.stock_item_dto.StockItemDto;
import org.project.mechanic_shop.domain.dto.stock_item_dto.StockItemManDto;
import org.project.mechanic_shop.domain.dto.stock_item_dto.StockItemShortDto;
import org.project.mechanic_shop.domain.entities.stock_item.StockItem;

@Mapper(componentModel = "spring")
public interface StockItemMapper {
	StockItem toEntity(StockItemManDto stockItemManDto);

	StockItemShortDto toShortDto(StockItem stockItem);

	StockItemDto toDto(StockItem stockItem);
}
