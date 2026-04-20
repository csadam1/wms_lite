package com.cherry.wms_lite.model.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "inventoryEntity")
    private StorageLocationEntity storageLocation;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "inventoryEntity")
    private ContainerEntity container;
}
