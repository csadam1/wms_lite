package com.cherry.wms_lite.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "StorageLocation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageLocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "inventory_id", nullable = false)
    private InventoryEntity inventoryEntity;
}
