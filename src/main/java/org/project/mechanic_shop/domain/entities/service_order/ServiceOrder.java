package org.project.mechanic_shop.domain.entities.service_order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.project.mechanic_shop.domain.entities.base_audit_entity.BaseAuditEntity;
import org.project.mechanic_shop.domain.entities.budget.Budget;
import org.project.mechanic_shop.domain.entities.service_order_labor.ServiceOrderLabor;
import org.project.mechanic_shop.domain.entities.service_order_stock_item.ServiceOrderStockItem;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.entities.vehicle.Vehicle;
import org.project.mechanic_shop.domain.enums.ServiceOrderStatusEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrder extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicle_id", nullable = false)
	private Vehicle vehicle;

	@Column(name = "customer_complaint", columnDefinition = "TEXT", nullable = false)
	private String customerComplaint;

	@Column(name = "odometer_reading")
	private Integer odometerReading;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mechanic_id")
	private User responsibleMechanic;

	@Column(name = "mechanic_diagnosis", columnDefinition = "TEXT")
	private String mechanicDiagnosis;

	@OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<ServiceOrderStockItem> stockItems = new ArrayList<>();

	@OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<ServiceOrderLabor> labors = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ServiceOrderStatusEnum status = ServiceOrderStatusEnum.RECEIVED;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "budget_id", referencedColumnName = "id")
	private Budget budget;

	@Column(name = "approval_date")
	private LocalDateTime approvalDate;

	@Column(name = "estimated_completion_date")
	private LocalDateTime estimatedCompletionDate;

	@Column(name = "estimated_completion_days")
	private Integer estimatedCompletionDays;

	@Column(name = "actual_completion_date")
	private LocalDateTime actualCompletionDate;

	@Column(name = "actual_completion_days")
	private Integer actualCompletionDays;

	public void addStockItem(ServiceOrderStockItem stockItem) {
		stockItems.add(stockItem);
		stockItem.setServiceOrder(this);
	}

	public void addLabor(ServiceOrderLabor labor) {
		labors.add(labor);
		labor.setServiceOrder(this);
	}
}
