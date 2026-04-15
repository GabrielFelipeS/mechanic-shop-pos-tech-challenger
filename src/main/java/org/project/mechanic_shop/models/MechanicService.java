package org.project.mechanic_shop.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "mechanic_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MechanicService extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_time_minutes", nullable = false)
    private Integer estimatedTimeMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}