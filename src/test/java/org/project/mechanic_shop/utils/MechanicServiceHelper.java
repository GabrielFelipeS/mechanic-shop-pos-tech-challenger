package org.project.mechanic_shop.utils;

import java.math.BigDecimal;
import org.project.mechanic_shop.domain.dto.mechanic_service_dto.MechanicServiceShortDto;
import org.project.mechanic_shop.domain.entities.mechanic_service.MechanicService;

public class MechanicServiceHelper {

	public static MechanicService generateMechanicService() {
		return new MechanicService(
			1L,
			"Troca de óleo",
			"Serviço de troca de óleo completo",
			60,
			new BigDecimal("150.00")
		);
	}

	public static MechanicService generateMechanicServiceWithoutId() {
		return new MechanicService(
			null,
			"Troca de óleo",
			"Serviço de troca de óleo completo",
			60,
			new BigDecimal("150.00")
		);
	}

	public static MechanicService generateMechanicService(
		Long id,
		String name,
		String description,
		Integer estimatedTimeMinutes,
		BigDecimal price
	) {
		return new MechanicService(id, name, description, estimatedTimeMinutes, price);
	}

	public static MechanicServiceShortDto generateMechanicServiceShortDto() {
		var mechanicService = generateMechanicService();
		return new MechanicServiceShortDto(
			mechanicService.getExternalId(),
			mechanicService.getName(),
			mechanicService.getEstimatedTimeMinutes(),
			mechanicService.getPrice()
		);
	}
}
