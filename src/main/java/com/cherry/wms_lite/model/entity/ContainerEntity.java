package com.cherry.wms_lite.model.entity;

import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContainerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "container_type_id", nullable = false)
    private ContainerTypeEntity containerType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "status", nullable = false)
    private ContainerStatusEnum status;

    @Column(name = "removed", nullable = false)
    private Boolean removed;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "inventory_entity_id", nullable = false)
    private InventoryEntity inventoryEntity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "attached_to_inventory_entity_id", nullable = false)
    private InventoryEntity attachedToInventoryEntity;
}
