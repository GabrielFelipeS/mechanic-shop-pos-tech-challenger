package org.project.mechanic_shop.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderLaborDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderShortDto;
import org.project.mechanic_shop.domain.dto.service_order_dto.ServiceOrderStockItemDto;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.service_order_labor.ServiceOrderLabor;
import org.project.mechanic_shop.domain.entities.service_order_stock_item.ServiceOrderStockItem;

@Mapper(componentModel = "spring", uses = { VehicleMapper.class, UserMapper.class })
public interface ServiceOrderMapper {
	ServiceOrderDto toDto(ServiceOrder entity);

	@Mapping(target = "licensePlate", source = "vehicle.licensePlate")
	@Mapping(target = "customerName", source = "vehicle.owner.name")
    ServiceOrderShortDto toShortDto(ServiceOrder entity);

	@Mapping(target = "partExternalId", source = "stockItem.externalId")
	@Mapping(target = "partName", source = "stockItem.name")
	@Mapping(target = "partCode", source = "stockItem.code")
    ServiceOrderStockItemDto toPartDto(ServiceOrderStockItem entity);

	@Mapping(target = "mechanicServiceExternalId", source = "mechanicService.externalId")
	@Mapping(target = "serviceName", source = "mechanicService.name")
    ServiceOrderLaborDto toLaborDto(ServiceOrderLabor entity);
}
