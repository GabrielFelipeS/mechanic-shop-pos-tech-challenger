package org.project.mechanic_shop.infrastructure.jpa.mappers;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.project.mechanic_shop.domain.entities.budget.Budget;
import org.project.mechanic_shop.domain.entities.service_order.ServiceOrder;
import org.project.mechanic_shop.domain.entities.service_order_labor.ServiceOrderLabor;
import org.project.mechanic_shop.domain.entities.service_order_stock_item.ServiceOrderStockItem;
import org.project.mechanic_shop.infrastructure.jpa.entities.BudgetJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.entities.ServiceOrderJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.entities.ServiceOrderLaborJpaEntity;
import org.project.mechanic_shop.infrastructure.jpa.entities.ServiceOrderStockItemJpaEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceOrderJpaMapper {

	private final VehicleJpaMapper vehicleJpaMapper;
	private final UserJpaMapper userJpaMapper;
	private final MechanicServiceJpaMapper mechanicServiceJpaMapper;
	private final StockItemJpaMapper stockItemJpaMapper;

	public ServiceOrder toDomain(ServiceOrderJpaEntity jpa) {
		if (jpa == null) return null;
		ServiceOrder so = new ServiceOrder();
		so.setExternalId(jpa.getExternalId());
		so.setCreatedAt(jpa.getCreatedAt());
		so.setCreatedFor(jpa.getCreatedFor());
		so.setLastUpdatedAt(jpa.getLastUpdatedAt());
		so.setLastUpdatedFor(jpa.getLastUpdatedFor());
		so.setId(jpa.getId());
		so.setVehicle(vehicleJpaMapper.toDomain(jpa.getVehicle()));
		so.setCustomerComplaint(jpa.getCustomerComplaint());
		so.setOdometerReading(jpa.getOdometerReading());
		so.setResponsibleMechanic(userJpaMapper.toDomain(jpa.getResponsibleMechanic()));
		so.setMechanicDiagnosis(jpa.getMechanicDiagnosis());
		so.setStatus(jpa.getStatus());
		so.setBudget(budgetToDomain(jpa.getBudget()));
		so.setApprovalDate(jpa.getApprovalDate());
		so.setEstimatedCompletionDate(jpa.getEstimatedCompletionDate());
		so.setEstimatedCompletionDays(jpa.getEstimatedCompletionDays());
		so.setActualCompletionDate(jpa.getActualCompletionDate());
		so.setActualCompletionDays(jpa.getActualCompletionDays());
		so.setApprovalToken(jpa.getApprovalToken());
		so.setLabors(laborsToDomain(jpa.getLabors()));
		so.setStockItems(stockItemsToDomain(jpa.getStockItems()));
		return so;
	}

	public ServiceOrderJpaEntity toJpa(ServiceOrder so) {
		if (so == null) return null;
		ServiceOrderJpaEntity jpa = new ServiceOrderJpaEntity();
		jpa.setExternalId(so.getExternalId());
		jpa.setCreatedAt(so.getCreatedAt());
		jpa.setCreatedFor(so.getCreatedFor());
		jpa.setLastUpdatedAt(so.getLastUpdatedAt());
		jpa.setLastUpdatedFor(so.getLastUpdatedFor());
		jpa.setId(so.getId());
		jpa.setVehicle(vehicleJpaMapper.toJpa(so.getVehicle()));
		jpa.setCustomerComplaint(so.getCustomerComplaint());
		jpa.setOdometerReading(so.getOdometerReading());
		jpa.setResponsibleMechanic(userJpaMapper.toJpa(so.getResponsibleMechanic()));
		jpa.setMechanicDiagnosis(so.getMechanicDiagnosis());
		jpa.setStatus(so.getStatus());
		jpa.setBudget(budgetToJpa(so.getBudget(), jpa));
		jpa.setApprovalDate(so.getApprovalDate());
		jpa.setEstimatedCompletionDate(so.getEstimatedCompletionDate());
		jpa.setEstimatedCompletionDays(so.getEstimatedCompletionDays());
		jpa.setActualCompletionDate(so.getActualCompletionDate());
		jpa.setActualCompletionDays(so.getActualCompletionDays());
		jpa.setApprovalToken(so.getApprovalToken());
		jpa.setLabors(laborsToJpa(so.getLabors(), jpa));
		jpa.setStockItems(stockItemsToJpa(so.getStockItems(), jpa));
		return jpa;
	}

	private Budget budgetToDomain(BudgetJpaEntity jpa) {
		if (jpa == null) return null;
		Budget b = new Budget();
		b.setId(jpa.getId());
		b.setExternalId(jpa.getExternalId());
		b.setTotalAmount(jpa.getTotalAmount());
		b.setStatus(jpa.getStatus());
		return b;
	}

	private BudgetJpaEntity budgetToJpa(Budget b, ServiceOrderJpaEntity owner) {
		if (b == null) return null;
		BudgetJpaEntity jpa = new BudgetJpaEntity();
		jpa.setId(b.getId());
		jpa.setExternalId(b.getExternalId());
		jpa.setTotalAmount(b.getTotalAmount());
		jpa.setStatus(b.getStatus());
		jpa.setServiceOrder(owner);
		return jpa;
	}

	private List<ServiceOrderLabor> laborsToDomain(List<ServiceOrderLaborJpaEntity> jpaList) {
		if (jpaList == null) return new ArrayList<>();
		List<ServiceOrderLabor> result = new ArrayList<>();
		for (ServiceOrderLaborJpaEntity jpa : jpaList) {
			ServiceOrderLabor labor = new ServiceOrderLabor();
			labor.setId(jpa.getId());
			labor.setMechanicService(mechanicServiceJpaMapper.toDomain(jpa.getMechanicService()));
			labor.setQuantity(jpa.getQuantity());
			labor.setUnitPrice(jpa.getUnitPrice());
			labor.setTotalPrice(jpa.getTotalPrice());
			result.add(labor);
		}
		return result;
	}

	private List<ServiceOrderLaborJpaEntity> laborsToJpa(List<ServiceOrderLabor> list, ServiceOrderJpaEntity owner) {
		if (list == null) return new ArrayList<>();
		List<ServiceOrderLaborJpaEntity> result = new ArrayList<>();
		for (ServiceOrderLabor labor : list) {
			ServiceOrderLaborJpaEntity jpa = new ServiceOrderLaborJpaEntity();
			jpa.setId(labor.getId());
			jpa.setServiceOrder(owner);
			jpa.setMechanicService(mechanicServiceJpaMapper.toJpa(labor.getMechanicService()));
			jpa.setQuantity(labor.getQuantity());
			jpa.setUnitPrice(labor.getUnitPrice());
			jpa.setTotalPrice(labor.getTotalPrice());
			result.add(jpa);
		}
		return result;
	}

	private List<ServiceOrderStockItem> stockItemsToDomain(List<ServiceOrderStockItemJpaEntity> jpaList) {
		if (jpaList == null) return new ArrayList<>();
		List<ServiceOrderStockItem> result = new ArrayList<>();
		for (ServiceOrderStockItemJpaEntity jpa : jpaList) {
			ServiceOrderStockItem item = new ServiceOrderStockItem();
			item.setId(jpa.getId());
			item.setStockItem(stockItemJpaMapper.toDomain(jpa.getStockItem()));
			item.setStockItemType(jpa.getStockItemType());
			item.setQuantity(jpa.getQuantity());
			item.setUnitPrice(jpa.getUnitPrice());
			item.setTotalPrice(jpa.getTotalPrice());
			result.add(item);
		}
		return result;
	}

	private List<ServiceOrderStockItemJpaEntity> stockItemsToJpa(List<ServiceOrderStockItem> list, ServiceOrderJpaEntity owner) {
		if (list == null) return new ArrayList<>();
		List<ServiceOrderStockItemJpaEntity> result = new ArrayList<>();
		for (ServiceOrderStockItem item : list) {
			ServiceOrderStockItemJpaEntity jpa = new ServiceOrderStockItemJpaEntity();
			jpa.setId(item.getId());
			jpa.setServiceOrder(owner);
			jpa.setStockItem(stockItemJpaMapper.toJpa(item.getStockItem()));
			jpa.setStockItemType(item.getStockItemType());
			jpa.setQuantity(item.getQuantity());
			jpa.setUnitPrice(item.getUnitPrice());
			jpa.setTotalPrice(item.getTotalPrice());
			result.add(jpa);
		}
		return result;
	}
}
