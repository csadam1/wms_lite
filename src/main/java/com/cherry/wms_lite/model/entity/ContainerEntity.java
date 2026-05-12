package com.cherry.wms_lite.model.entity;

import com.cherry.wms_lite.model.enumerate.ContainerStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "Container")
@Getter
@Setter
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
    @JoinColumn(name = "inventory_id", nullable = false)
    private InventoryEntity inventory;

    @ManyToOne(optional = false)
    @JoinColumn(name = "attached_to_inventory_id", nullable = false)
    private InventoryEntity attachedToInventory;
}
