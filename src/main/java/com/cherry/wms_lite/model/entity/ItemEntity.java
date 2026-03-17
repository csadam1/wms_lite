package com.cherry.wms_lite.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "attached_to_inventory_entity_id", nullable = false)
    private InventoryEntity attachedToInventoryEntity;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;
}
