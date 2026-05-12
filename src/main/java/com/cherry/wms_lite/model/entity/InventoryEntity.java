package com.cherry.wms_lite.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(mappedBy = "attachedToInventory", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "attachedToInventory", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ContainerEntity> containers = new ArrayList<>();

    @OneToOne(mappedBy = "inventory")
    private StorageLocationEntity storageLocation;

    @OneToOne(mappedBy = "inventory")
    private ContainerEntity container;
}
