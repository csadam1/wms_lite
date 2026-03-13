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

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "container_type_id", nullable = false)
    private ContainerTypeEntity containerType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "status", nullable = false)
    private Enum<ContainerStatusEnum> status;
}
