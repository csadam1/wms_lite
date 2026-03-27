package com.cherry.wms_lite.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(mappedBy = "attachedToInventoryEntity", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "attachedToInventoryEntity", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ContainerEntity> containers = new ArrayList<>();

    @OneToOne(mappedBy = "inventoryEntity")
    private StorageLocationEntity storageLocation;

    @OneToOne(mappedBy = "inventoryEntity")
    private ContainerEntity container;

}
