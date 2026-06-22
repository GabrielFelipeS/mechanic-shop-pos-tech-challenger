package org.project.mechanic_shop.domain.entities.service_order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.domain.entities.base_audit_entity.BaseAuditEntity;
import org.project.mechanic_shop.domain.entities.budget.Budget;
import org.project.mechanic_shop.domain.entities.service_order_labor.ServiceOrderLabor;
import org.project.mechanic_shop.domain.entities.service_order_stock_item.ServiceOrderStockItem;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrder extends BaseAuditEntity {
	private Long id;
	private Vehicle vehicle;
	private String customerComplaint;
	private Integer odometerReading;
	private User responsibleMechanic;
	private String mechanicDiagnosis;
	private List<ServiceOrderStockItem> stockItems = new ArrayList<>();
	private List<ServiceOrderLabor> labors = new ArrayList<>();
	private ServiceOrderStatusEnum status = ServiceOrderStatusEnum.RECEIVED;
	private Budget budget;
	private LocalDateTime approvalDate;
	private LocalDateTime estimatedCompletionDate;
	private Integer estimatedCompletionDays;
	private LocalDateTime actualCompletionDate;
	private Integer actualCompletionDays;
	private String approvalToken;

	public void addStockItem(ServiceOrderStockItem stockItem) {
		stockItems.add(stockItem);
	}

	public void addLabor(ServiceOrderLabor labor) {
		labors.add(labor);
	}
}
