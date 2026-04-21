package org.project.mechanic_shop.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;

import java.math.BigDecimal;
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
    private List<ServiceOrderStockItem> stockItems = new ArrayList<>();

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOrderLabor> labors = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceOrderStatusEnum status = ServiceOrderStatusEnum.RECEIVED;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    public void addStockItem(ServiceOrderStockItem stockItem) {
        stockItems.add(stockItem);
        stockItem.setServiceOrder(this);
    }

    public void addLabor(ServiceOrderLabor labor) {
        labors.add(labor);
        labor.setServiceOrder(this);
    }
}